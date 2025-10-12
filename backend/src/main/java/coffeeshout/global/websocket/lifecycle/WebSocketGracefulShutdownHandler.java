package coffeeshout.global.websocket.lifecycle;

import coffeeshout.global.websocket.StompSessionManager;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * WebSocket Graceful Shutdown 핸들러
 * <p>
 * Spring Boot 애플리케이션 종료 시 활성 WebSocket 연결이 모두 종료될 때까지 대기합니다.
 * 최대 5분간 대기하며, 모든 연결이 종료되면 즉시 shutdown을 완료합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketGracefulShutdownHandler implements SmartLifecycle {

    private final StompSessionManager sessionManager;

    private volatile boolean isRunning = false;
    /**
     * -- GETTER --
     *  현재 Shutdown 중인지 여부 반환
     */
    @Getter
    private volatile boolean isShuttingDown = false;
    private CompletableFuture<Void> shutdownFuture = null;

    @Override
    public void start() {
        isRunning = true;
        log.info("▶️ WebSocketGracefulShutdownHandler 시작됨");
    }

    @Override
    public void stop() {
        stop(() -> {});
    }

    @Override
    public void stop(@NonNull Runnable callback) {
        log.info("🛑 WebSocket Graceful Shutdown 시작");

        int currentConnections = sessionManager.getTotalConnectedClientCount();

        // 활성 연결이 없으면 즉시 종료
        if (currentConnections == 0) {
            log.info("✅ 활성 WebSocket 연결 없음. 즉시 종료");
            isRunning = false;
            callback.run();
            return;
        }

        // Shutdown 모드 활성화
        isShuttingDown = true;
        shutdownFuture = new CompletableFuture<>();

        log.info("⏳ {} 개의 활성 WebSocket 연결 종료 대기 중... (최대 5분)", currentConnections);

        // 주기적인 상태 로깅 스케줄링
        scheduleStatusLogging();

        // 타임아웃과 함께 대기 (이벤트 기반 - CompletableFuture 사용)
        try {
            shutdownFuture.get(5, TimeUnit.MINUTES);
            log.info("✅ 모든 WebSocket 연결 정상 종료 완료");
        } catch (TimeoutException e) {
            int remaining = sessionManager.getTotalConnectedClientCount();
            log.warn("⚠️ Graceful Shutdown 타임아웃 (5분): 활성 연결 {} 개가 남아있습니다. 강제 종료합니다.", remaining);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Graceful Shutdown 중단됨", e);
        } catch (Exception e) {
            log.error("❌ Graceful Shutdown 중 예외 발생", e);
        } finally {
            isShuttingDown = false;
            isRunning = false;
            shutdownFuture = null;
            callback.run();
        }
    }

    /**
     * SessionDisconnectEvent 발생 시 호출되는 메서드
     * <p>
     * 활성 연결 수를 체크하고, 모든 연결이 종료되었으면 CompletableFuture를 완료시킵니다.
     * </p>
     */
    public void onSessionDisconnected() {
        if (!isShuttingDown || shutdownFuture == null) {
            return;
        }

        int remaining = sessionManager.getTotalConnectedClientCount();
        log.debug("세션 종료 감지: 남은 연결 {} 개", remaining);

        if (remaining == 0) {
            log.info("🎉 마지막 WebSocket 연결 종료 감지! Graceful Shutdown 완료");
            shutdownFuture.complete(null);
        }
    }

    /**
     * Graceful Shutdown 진행 상황을 주기적으로 로깅
     */
    private void scheduleStatusLogging() {
        CompletableFuture.runAsync(() -> {
            while (shutdownFuture != null && !shutdownFuture.isDone()) {
                try {
                    Thread.sleep(10_000); // 10초마다 로깅
                    if (shutdownFuture != null && !shutdownFuture.isDone()) {
                        int remaining = sessionManager.getTotalConnectedClientCount();
                        log.info("📊 Graceful Shutdown 진행 중: 남은 연결 {} 개", remaining);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPhase() {
        // SmartLifecycle의 phase 값
        // 값이 클수록 나중에 종료됨 (WebSocket은 가장 마지막에 종료되어야 함)
        return Integer.MAX_VALUE;
    }
}

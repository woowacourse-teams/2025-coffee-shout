package coffeeshout.global.websocket.lifecycle;

import coffeeshout.global.websocket.event.SessionCountChangedEvent;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
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
public class WebSocketGracefulShutdownHandler implements SmartLifecycle {

    private final WebSocketSessionTracker sessionTracker;
    private final TaskScheduler taskScheduler;

    public WebSocketGracefulShutdownHandler(WebSocketSessionTracker sessionTracker, @Qualifier("delayRemovalScheduler") TaskScheduler taskScheduler) {
        this.sessionTracker = sessionTracker;
        this.taskScheduler = taskScheduler;
    }

    private volatile boolean isRunning = false;
    /**
     * -- GETTER --
     *  현재 Shutdown 중인지 여부 반환
     */
    @Getter
    private volatile boolean isShuttingDown = false;
    private CompletableFuture<Void> shutdownFuture = null;
    private ScheduledFuture<?> statusCheckTask = null;

    @Value("${spring.lifecycle.timeout-per-shutdown-phase}")
    private Duration shutdownWaitDuration;

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

        int currentConnections = sessionTracker.getActiveSessionCount();

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
            shutdownFuture.get(shutdownWaitDuration.toMinutes(), TimeUnit.MINUTES);
            log.info("✅ 모든 WebSocket 연결 정상 종료 완료");
        } catch (TimeoutException e) {
            int remaining = sessionTracker.getActiveSessionCount();
            log.warn("⚠️ Graceful Shutdown 타임아웃 (5분): 활성 연결 {} 개가 남아있습니다. 강제 종료합니다.", remaining);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Graceful Shutdown 중단됨", e);
        } catch (Exception e) {
            log.error("❌ Graceful Shutdown 중 예외 발생", e);
        } finally {
            cancelStatusCheckTask();
            isShuttingDown = false;
            isRunning = false;
            shutdownFuture = null;
            callback.run();
        }
    }

    /**
     * 세션 수 변경 이벤트 수신
     * <p>
     * 세션이 해제될 때마다 호출되어, 모든 연결이 종료되었는지 확인합니다.
     * </p>
     */
    @EventListener
    public void onSessionCountChanged(SessionCountChangedEvent event) {
        // Shutdown 모드가 아니거나, CONNECTED 이벤트는 무시
        if (!isShuttingDown || shutdownFuture == null
                || event.getChangeType() == SessionCountChangedEvent.ChangeType.CONNECTED) {
            return;
        }

        int remaining = event.getRemainingSessionCount();
        log.debug("세션 종료 감지: 남은 연결 {} 개", remaining);

        if (remaining == 0) {
            log.info("🎉 마지막 WebSocket 연결 종료 감지! Graceful Shutdown 완료");
            shutdownFuture.complete(null);
        }
    }

    /**
     * Graceful Shutdown 진행 상황을 주기적으로 로깅 (5초마다)
     * Spring TaskScheduler를 사용하여 안정적으로 체크
     */
    private void scheduleStatusLogging() {
        statusCheckTask = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                if (shutdownFuture == null || shutdownFuture.isDone()) {
                    return;
                }

                int remaining = sessionTracker.getActiveSessionCount();
                log.info("📊 Graceful Shutdown 진행 중: 남은 연결 {} 개", remaining);

                // 안전장치: 세션이 0개인데 CompletableFuture가 완료되지 않은 경우
                if (remaining == 0 && !shutdownFuture.isDone()) {
                    log.warn("⚠️ 세션이 0개인데 종료되지 않음. 강제로 완료 처리합니다");
                    shutdownFuture.complete(null);
                }
            } catch (Exception e) {
                log.error("❌ Graceful Shutdown 상태 체크 중 오류", e);
            }
        }, Duration.ofSeconds(5)); // 5초마다 반복
    }

    /**
     * 상태 체크 작업 취소
     */
    private void cancelStatusCheckTask() {
        if (statusCheckTask != null && !statusCheckTask.isCancelled()) {
            statusCheckTask.cancel(false);
            statusCheckTask = null;
        }
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

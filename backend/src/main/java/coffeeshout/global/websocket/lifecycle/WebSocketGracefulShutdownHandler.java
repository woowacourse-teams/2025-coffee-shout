package coffeeshout.global.websocket.lifecycle;

import coffeeshout.global.websocket.event.SessionCountChangedEvent;
import coffeeshout.global.websocket.event.SessionCountChangedEvent.SessionChangeType;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.Getter;
import lombok.NonNull;
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
 * 설정된 시간까지 대기하며, 모든 연결이 종료되면 즉시 shutdown을 완료합니다.
 * </p>
 */
@Slf4j
@Component
public class WebSocketGracefulShutdownHandler implements SmartLifecycle {

    private static final Duration STATUS_CHECK_INTERVAL = Duration.ofSeconds(5);

    private final WebSocketSessionTracker sessionTracker;
    private final TaskScheduler taskScheduler;
    private final Duration shutdownWaitDuration;

    private volatile boolean isRunning = false;
    @Getter
    private volatile boolean isShuttingDown = false;
    private CompletableFuture<Void> shutdownFuture;
    private ScheduledFuture<?> statusCheckTask;

    public WebSocketGracefulShutdownHandler(
            WebSocketSessionTracker sessionTracker,
            @Qualifier("delayRemovalScheduler") TaskScheduler taskScheduler,
            @Value("${spring.lifecycle.timeout-per-shutdown-phase}") Duration shutdownWaitDuration) {
        this.sessionTracker = sessionTracker;
        this.taskScheduler = taskScheduler;
        this.shutdownWaitDuration = shutdownWaitDuration;
    }

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

        final int currentConnections = sessionTracker.getActiveSessionCount();

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

        final long timeoutSeconds = shutdownWaitDuration.toSeconds();
        final long displayMinutes = timeoutSeconds / 60;
        final long displaySeconds = timeoutSeconds % 60;
        log.info("⏳ {} 개의 활성 WebSocket 연결 종료 대기 중... (최대 {}분 {}초)", currentConnections, displayMinutes, displaySeconds);

        // 주기적인 상태 로깅 스케줄링
        scheduleStatusLogging();

        // 타임아웃과 함께 대기 (이벤트 기반 - CompletableFuture 사용)
        try {
            shutdownFuture.get(shutdownWaitDuration.toMillis(), TimeUnit.MILLISECONDS);
            log.info("✅ 모든 WebSocket 연결 정상 종료 완료");
        } catch (TimeoutException e) {
            final int remaining = sessionTracker.getActiveSessionCount();
            log.warn("⚠️ Graceful Shutdown 타임아웃 ({}분 {}초): 활성 연결 {} 개가 남아있습니다. 강제 종료합니다.",
                    displayMinutes, displaySeconds, remaining);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Graceful Shutdown 중단됨", e);
        } catch (Exception e) {
            log.error("❌ Graceful Shutdown 중 예외 발생", e);
        } finally {
            cleanup();
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
        // DISCONNECTED 이벤트만 처리
        if (event.changeType() != SessionChangeType.DISCONNECTED) {
            return;
        }

        // Shutdown 모드가 아니면 무시
        final CompletableFuture<Void> future = shutdownFuture;
        if (!isShuttingDown || future == null) {
            return;
        }

        final int remaining = event.remainingSessionCount();
        log.debug("세션 종료 감지: 남은 연결 {} 개", remaining);

        if (remaining == 0 && !future.isDone()) {
            log.info("🎉 마지막 WebSocket 연결 종료 감지! Graceful Shutdown 완료");
            future.complete(null);
        }
    }

    /**
     * Graceful Shutdown 진행 상황을 주기적으로 로깅
     * <p>
     * STATUS_CHECK_INTERVAL마다 남은 연결 수를 확인하고,
     * 안전장치로 세션이 0개인데 완료되지 않은 경우 강제 완료합니다.
     * </p>
     */
    private void scheduleStatusLogging() {
        statusCheckTask = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                final CompletableFuture<Void> future = shutdownFuture;
                if (future == null || future.isDone()) {
                    return;
                }

                final int remaining = sessionTracker.getActiveSessionCount();
                log.info("📊 Graceful Shutdown 진행 중: 남은 연결 {} 개", remaining);

                // 안전장치: 세션이 0개인데 CompletableFuture가 완료되지 않은 경우
                if (remaining == 0 && !future.isDone()) {
                    log.warn("⚠️ 세션이 0개인데 종료되지 않음. 강제로 완료 처리합니다");
                    future.complete(null);
                }
            } catch (Exception e) {
                log.error("❌ Graceful Shutdown 상태 체크 중 오류", e);
            }
        }, STATUS_CHECK_INTERVAL);
    }

    /**
     * Graceful Shutdown 정리 작업
     */
    private void cleanup() {
        cancelStatusCheckTask();
        isShuttingDown = false;
        isRunning = false;
        shutdownFuture = null;
    }

    /**
     * 상태 체크 작업 취소
     */
    private void cancelStatusCheckTask() {
        final ScheduledFuture<?> task = statusCheckTask;
        if (task != null && !task.isCancelled()) {
            task.cancel(false);
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

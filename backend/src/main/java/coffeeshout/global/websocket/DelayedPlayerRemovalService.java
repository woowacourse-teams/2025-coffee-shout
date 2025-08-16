package coffeeshout.global.websocket;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DelayedPlayerRemovalService {

    private static final Duration REMOVAL_DELAY = Duration.ofSeconds(15);

    @Qualifier("delayRemovalScheduler")
    private final TaskScheduler taskScheduler;
    private final PlayerDisconnectionService playerDisconnectionService;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public void schedulePlayerRemoval(String playerKey, String sessionId, String reason) {
        log.info("플레이어 지연 삭제 스케줄링: playerKey={}, sessionId={}, delay={}초",
                playerKey, sessionId, REMOVAL_DELAY.getSeconds());
        
        // 새로운 스케줄 등록
        final ScheduledFuture<?> future = taskScheduler.schedule(
                () -> executePlayerRemoval(playerKey, sessionId, reason),
                Instant.now().plus(REMOVAL_DELAY)
        );

        scheduledTasks.put(playerKey, future);
    }

    public void cancelScheduledRemoval(String playerKey) {
        final ScheduledFuture<?> future = scheduledTasks.remove(playerKey);
        if (future != null && !future.isDone()) {
            future.cancel(false);
            log.info("플레이어 지연 삭제 취소됨: playerKey={}", playerKey);
        }
    }

    private void executePlayerRemoval(String playerKey, String sessionId, String reason) {
        try {
            log.info("플레이어 지연 삭제 실행: playerKey={}, sessionId={}, reason={}",
                    playerKey, sessionId, reason);

            playerDisconnectionService.handlePlayerDisconnection(playerKey, sessionId, reason);
            scheduledTasks.remove(playerKey);

        } catch (Exception e) {
            log.error("플레이어 지연 삭제 실행 중 오류 발생: playerKey={}, error={}",
                    playerKey, e.getMessage(), e);
        }
    }


}

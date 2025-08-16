package coffeeshout.room.application;

import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RoomCleanupService {

    private final long roomCleanupDelayMs;
    private final RoomQueryService roomQueryService;
    private final RoomCommandService roomCommandService;
    private final StompSessionManager stompSessionManager;
    private final TaskScheduler taskScheduler;

    public RoomCleanupService(
            @Value("${room.cleanup.interval}") long roomCleanupDelayMs,
            RoomQueryService roomQueryService,
            RoomCommandService roomCommandService,
            StompSessionManager stompSessionManager,
            TaskScheduler taskScheduler) {
        this.roomCleanupDelayMs = roomCleanupDelayMs;
        this.roomQueryService = roomQueryService;
        this.roomCommandService = roomCommandService;
        this.stompSessionManager = stompSessionManager;
        this.taskScheduler = taskScheduler;
    }

    public void delayCleanUp(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        taskScheduler.schedule(() -> roomCommandService.delete(room),
                Instant.now().plus(Duration.ofMillis(roomCleanupDelayMs)));
    }

    @PostConstruct
    public void startScheduledCleanup() {
        taskScheduler.scheduleWithFixedDelay(this::cleanupEmptyRooms,
                Duration.ofMillis(roomCleanupDelayMs));
    }

    private void cleanupEmptyRooms() {
        log.info("빈 방 정리 작업 시작");
        for (Room room : roomQueryService.getAll()) {
            final JoinCode joinCode = room.getJoinCode();

            if (!stompSessionManager.hasActiveConnections(joinCode)) {
                log.debug("JoinCode[{}] 방에 연결된 사용자가 없어 정리합니다.", joinCode.value());
                roomCommandService.delete(room);
            }
        }
    }
}

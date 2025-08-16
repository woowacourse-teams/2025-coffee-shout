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

    private final Duration roomCleanupInterval;
    private final boolean cleanupEnabled;
    private final RoomQueryService roomQueryService;
    private final RoomCommandService roomCommandService;
    private final StompSessionManager stompSessionManager;
    private final TaskScheduler taskScheduler;

    public RoomCleanupService(
            @Value("${room.cleanup.interval}") Duration roomCleanupInterval,
            @Value("${room.cleanup.enabled:true}") boolean cleanupEnabled,
            RoomQueryService roomQueryService,
            RoomCommandService roomCommandService,
            StompSessionManager stompSessionManager,
            TaskScheduler taskScheduler) {
        this.roomCleanupInterval = roomCleanupInterval;
        this.cleanupEnabled = cleanupEnabled;
        this.roomQueryService = roomQueryService;
        this.roomCommandService = roomCommandService;
        this.stompSessionManager = stompSessionManager;
        this.taskScheduler = taskScheduler;
    }

    public void delayCleanUp(String joinCode) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

        taskScheduler.schedule(() -> roomCommandService.delete(room),
                Instant.now().plus(roomCleanupInterval));
    }

    @PostConstruct
    public void startScheduledCleanup() {
        log.debug("RoomCleanupService 설정 - enabled: {}, interval: {}", cleanupEnabled, roomCleanupInterval);
        
        if (cleanupEnabled) {
            log.info("방 정리 스케줄러 시작 - 간격: {}", roomCleanupInterval);
            taskScheduler.scheduleWithFixedDelay(this::cleanupEmptyRooms, roomCleanupInterval);
            return;
        }

        log.info("방 정리 스케줄러 비활성화됨 (room.cleanup.enabled=false)");
    }

    // TODO 제거되는 방 개수 메트릭화하면 좋을 거 같음
    private void cleanupEmptyRooms() {
        log.info("빈 방 정리 작업 시작");
        for (Room room : roomQueryService.getAll()) {
            final JoinCode joinCode = room.getJoinCode();

            if (!stompSessionManager.hasActiveConnections(joinCode)) {
                log.info("JoinCode[{}] 방에 연결된 사용자가 없어 정리합니다.", joinCode.value());
                roomCommandService.delete(room);
            }
        }
    }
}

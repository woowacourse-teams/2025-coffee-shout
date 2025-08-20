package coffeeshout.room.application;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.service.RoomCommandService;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DelayedRoomRemovalService {

    private final TaskScheduler taskScheduler;
    private final Duration removeDuration;
    private final RoomCommandService roomCommandService;

    public DelayedRoomRemovalService(
            @Qualifier("delayRemovalScheduler") TaskScheduler taskScheduler,
            @Value("${room.removalDelay}") Duration removalDelay,
            RoomCommandService roomCommandService) {
        this.taskScheduler = taskScheduler;
        this.removeDuration = removalDelay;
        this.roomCommandService = roomCommandService;
    }

    public void scheduleRemoveRoom(JoinCode joinCode) {
        log.info("방 지연 삭제 스케줄링: joinCode={}, delay={}초",
                joinCode.value(), removeDuration.getSeconds());

        taskScheduler.schedule(() -> {
            roomCommandService.delete(joinCode);
            log.info("방 삭제 완료: joinCode={}", joinCode.value());
        }, Instant.now().plus(removeDuration));
    }
}

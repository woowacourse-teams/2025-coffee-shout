package coffeeshout.room.infra.handler;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.infra.RoomEventWaitManager;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomCreateEventHandler implements RoomEventHandler<RoomCreateEvent> {

    private final RoomService roomService;
    private final RoomEventWaitManager roomEventWaitManager;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void handle(RoomCreateEvent event) {
        try {
            log.info("방 생성 이벤트 수신: eventId={}, hostName={}, joinCode={}",
                    event.getEventId(), event.hostName(), event.joinCode());

            final Room room = createRoomInMemory(event);
            
            tryDbSave(event, room);

            roomEventWaitManager.notifySuccess(event.getEventId(), room);

        } catch (Exception e) {
            log.error("방 생성 이벤트 처리 실패", e);
            roomEventWaitManager.notifyFailure(event.getEventId(), e);
        }
    }

    private Room createRoomInMemory(RoomCreateEvent event) {
        return roomService.createRoomInternal(
                event.hostName(),
                event.selectedMenuRequest(),
                event.joinCode()
        );
    }

    private void tryDbSave(RoomCreateEvent event, Room room) {
        final String lockKey = "event:lock:" + event.getEventId();
        final String doneKey = "event:done:" + event.getEventId();

        if (isAlreadyProcessed(doneKey, event.getEventId())) {
            roomEventWaitManager.notifySuccess(event.getEventId(), room);
            return;
        }

        if (!acquireLock(lockKey, event.getEventId())) {
            roomEventWaitManager.notifySuccess(event.getEventId(), room);
            return;
        }

        try {
            saveToDatabase(event, doneKey);
        } finally {
            releaseLock(lockKey);
        }
    }

    private boolean isAlreadyProcessed(String doneKey, String eventId) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(doneKey))) {
            log.debug("이미 처리된 이벤트 (DB 저장 스킵): eventId={}", eventId);
            return true;
        }
        return false;
    }

    private boolean acquireLock(String lockKey, String eventId) {
        final Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", Duration.ofSeconds(5));

        if (!Boolean.TRUE.equals(acquired)) {
            log.debug("다른 인스턴스가 DB 저장 중: eventId={}", eventId);
            return false;
        }
        return true;
    }

    private void saveToDatabase(RoomCreateEvent event, String doneKey) {
        if (isAlreadyProcessed(doneKey, event.getEventId())) {
            return;
        }

        roomService.saveRoomEntity(event.joinCode());

        redisTemplate.opsForValue()
                .set(doneKey, "done", Duration.ofMinutes(10));

        log.info("방 생성 이벤트 처리 완료 (DB 저장): eventId={}, joinCode={}",
                event.getEventId(), event.joinCode());
    }

    private void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.ROOM_CREATE;
    }
}

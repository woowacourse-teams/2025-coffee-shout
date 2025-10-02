package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.lock.RedisLock;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.infra.messaging.RoomEventWaitManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomCreateEventHandler implements RoomEventHandler<RoomCreateEvent> {

    private final RoomService roomService;
    private final RoomEventWaitManager roomEventWaitManager;

    @Override
    public void handle(RoomCreateEvent event) {
        try {
            log.info("방 생성 이벤트 수신: eventId={}, hostName={}, joinCode={}",
                    event.eventId(), event.hostName(), event.joinCode());

            final Room room = createRoomInMemory(event);

            tryDbSave(event);

            roomEventWaitManager.notifySuccess(event.eventId(), room);

        } catch (Exception e) {
            log.error("방 생성 이벤트 처리 실패", e);
            roomEventWaitManager.notifyFailure(event.eventId(), e);
        }
    }

    private Room createRoomInMemory(RoomCreateEvent event) {
        return roomService.createRoomInternal(
                event.hostName(),
                event.selectedMenuRequest(),
                event.joinCode()
        );
    }

    @RedisLock(
            key = "#event.eventId()",
            lockPrefix = "event:lock:",
            donePrefix = "event:done:",
            waitTime = 0,
            leaseTime = 5000
    )
    private void tryDbSave(RoomCreateEvent event) {
        roomService.saveRoomEntity(event.joinCode());
        log.info("방 생성 이벤트 처리 완료 (DB 저장): eventId={}, joinCode={}",
                event.eventId(), event.joinCode());
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.ROOM_CREATE;
    }
}

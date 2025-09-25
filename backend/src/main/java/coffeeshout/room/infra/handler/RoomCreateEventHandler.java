package coffeeshout.room.infra.handler;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.infra.RoomEventWaitManager;
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
                    event.getEventId(), event.hostName(), event.joinCode());

            final Room room = roomService.createRoomInternal(
                    event.hostName(),
                    event.selectedMenuRequest(),
                    event.joinCode()
            );

            roomEventWaitManager.notifySuccess(event.getEventId(), room);

            log.info("방 생성 이벤트 처리 완료: eventId={}, joinCode={}", 
                    event.getEventId(), event.joinCode());

        } catch (Exception e) {
            log.error("방 생성 이벤트 처리 실패", e);
            roomEventWaitManager.notifyFailure(event.getEventId(), e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.ROOM_CREATE;
    }
}

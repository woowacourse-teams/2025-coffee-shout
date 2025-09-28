package coffeeshout.room.infra.handler;

import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RoomJoinEvent;
import coffeeshout.room.infra.RoomEventWaitManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomJoinEventHandler implements RoomEventHandler<RoomJoinEvent> {

    private final RoomService roomService;
    private final RoomEventWaitManager roomEventWaitManager;

    @Override
    public void handle(RoomJoinEvent event) {
        try {
            log.info("방 참가 이벤트 수신: eventId={}, joinCode={}, guestName={}",
                    event.getEventId(), event.getJoinCode(), event.getGuestName());

            final Room room = roomService.enterRoomInternal(
                    event.getJoinCode(),
                    event.getGuestName(),
                    event.getSelectedMenuRequest(),
                    event.getGuestColorIndex()
            );

            roomEventWaitManager.notifySuccess(event.getEventId(), room);

            log.info("방 참가 이벤트 처리 완료: eventId={}, joinCode={}, guestName={}",
                    event.getEventId(), event.getJoinCode(), event.getGuestName());

        } catch (Exception e) {
            log.error("방 참가 이벤트 처리 실패", e);
            roomEventWaitManager.notifyFailure(event.getEventId(), e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.ROOM_JOIN;
    }
}

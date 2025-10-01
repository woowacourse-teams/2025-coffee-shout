package coffeeshout.room.infra.messaging.handler;

import coffeeshout.room.application.DelayedRoomRemovalService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.event.RoomCreateEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.MenuCommandService;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomCreateEventHandler implements RoomEventHandler<RoomCreateEvent> {

    private final DelayedRoomRemovalService delayedRoomRemovalService;
    private final RoomCommandService roomCommandService;
    private final MenuCommandService menuCommandService;

    @Override
    public void handle(RoomCreateEvent event) {
        try {
            String joinCode = event.joinCode();
            log.info("방 생성 이벤트 수신: eventId={}, hostName={}, joinCode={}",
                    event.eventId(), event.hostName(), joinCode);

            SelectedMenuRequest selectedMenuRequest = event.selectedMenuRequest();
            Menu menu = menuCommandService.convertMenu(selectedMenuRequest.id(), selectedMenuRequest.customName());

            roomCommandService.createRoom(
                    new JoinCode(joinCode),
                    new PlayerName(event.hostName()),
                    menu,
                    selectedMenuRequest.temperature(),
                    event.qrCodeUrl()
            );

            log.info("방 생성 이벤트 처리 완료: eventId={}, joinCode={}",
                    event.eventId(), joinCode);

            delayedRoomRemovalService.scheduleRemoveRoom(new JoinCode(joinCode));
        } catch (Exception e) {
            log.error("방 생성 이벤트 처리 실패", e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.ROOM_CREATE;
    }
}

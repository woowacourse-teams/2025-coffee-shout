package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.room.application.DelayedRoomRemovalService;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.event.RoomCreateEvent;
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
public class RoomCreateEventHandler implements EventHandler {

    private final DelayedRoomRemovalService delayedRoomRemovalService;
    private final RoomCommandService roomCommandService;
    private final MenuCommandService menuCommandService;
    private final RoomService roomService;

    @Override
    public void handle(BaseEvent event) {
        final RoomCreateEvent roomCreateEvent = (RoomCreateEvent) event;
        final SelectedMenuRequest selectedMenuRequest = roomCreateEvent.selectedMenuRequest();
        final Menu menu = menuCommandService.convertMenu(selectedMenuRequest.id(), selectedMenuRequest.customName());

        roomCommandService.saveIfAbsentRoom(
                new JoinCode(roomCreateEvent.joinCode()),
                new PlayerName(roomCreateEvent.hostName()),
                menu,
                selectedMenuRequest.temperature()
        );

        delayedRoomRemovalService.scheduleRemoveRoom(new JoinCode(roomCreateEvent.joinCode()));
    }

    @Override
    public Class<?> eventType() {
        return RoomCreateEvent.class;
    }
}

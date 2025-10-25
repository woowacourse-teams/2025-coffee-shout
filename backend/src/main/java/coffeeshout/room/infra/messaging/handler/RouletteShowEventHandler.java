package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RouletteShowEvent;
import coffeeshout.room.ui.response.RoomStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouletteShowEventHandler implements EventHandler {

    private final RoomService roomService;
    private final RouletteEventDbService rouletteEventDbService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(BaseEvent event) {
        final RouletteShowEvent rouletteShowEvent = (RouletteShowEvent) event;
        final Room room = roomService.showRoulette(rouletteShowEvent.joinCode());
        final RoomStatusResponse response = RoomStatusResponse.of(room.getJoinCode(), room.getRoomState());

        messagingTemplate.convertAndSend("/topic/room/" + rouletteShowEvent.joinCode() + "/roulette",
                WebSocketResponse.success(response));

        rouletteEventDbService.saveRoomStatus(rouletteShowEvent);
    }

    @Override
    public Class<?> eventType() {
        return RouletteShowEvent.class;
    }
}

package coffeeshout.room.infra.handler;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.RouletteShowEvent;
import coffeeshout.room.ui.response.RoomStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouletteShowEventHandler implements RoomEventHandler<RouletteShowEvent> {

    private final RoomService roomService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(RouletteShowEvent event) {
        try {
            log.info("룰렛 전환 이벤트 수신: eventId={}, joinCode={}", event.eventId(), event.joinCode());

            final Room room = roomService.showRoulette(event.joinCode());
            final RoomStatusResponse response = RoomStatusResponse.of(room.getJoinCode(), room.getRoomState());

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode() + "/roulette",
                    WebSocketResponse.success(response));

        } catch (Exception e) {
            log.error("룰렛 전환 이벤트 처리 시래", e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.ROULETTE_SHOW;
    }
}

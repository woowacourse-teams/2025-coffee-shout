package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.domain.event.RouletteSpinEvent;
import coffeeshout.room.domain.player.Winner;
import coffeeshout.room.ui.response.WinnerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouletteSpinEventHandler implements EventHandler {

    private final LoggingSimpMessagingTemplate messagingTemplate;
    private final RoulettePersistenceService roulettePersistenceService;

    @Override
    public void handle(BaseEvent event) {
        final RouletteSpinEvent rouletteSpinEvent = (RouletteSpinEvent) event;
        final Winner winner = rouletteSpinEvent.winner();
        final WinnerResponse response = WinnerResponse.from(winner);

        messagingTemplate.convertAndSend("/topic/room/" + rouletteSpinEvent.joinCode() + "/winner",
                WebSocketResponse.success(response));
        roulettePersistenceService.saveRouletteResult(rouletteSpinEvent);
    }

    @Override
    public Class<RouletteSpinEvent> eventType() {
        return RouletteSpinEvent.class;
    }
}

package coffeeshout.global.websocket.infra.handler;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.event.player.PlayerDisconnectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerDisconnectedEventHandler implements EventHandler {

    private final DelayedPlayerRemovalService delayedPlayerRemovalService;

    @Override
    public void handle(BaseEvent event) {
        final PlayerDisconnectedEvent playerDisconnectedEvent = (PlayerDisconnectedEvent) event;
        delayedPlayerRemovalService.schedulePlayerRemoval(
                playerDisconnectedEvent.playerKey(),
                playerDisconnectedEvent.sessionId(),
                playerDisconnectedEvent.reason()
        );
    }

    @Override
    public Class<?> eventType() {
        return PlayerDisconnectedEvent.class;
    }
}

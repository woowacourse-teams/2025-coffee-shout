package coffeeshout.global.websocket.infra.handler;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.event.player.PlayerReconnectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerReconnectedEventHandler implements EventHandler {

    private final DelayedPlayerRemovalService delayedPlayerRemovalService;

    @Override
    public void handle(BaseEvent event) {
        final PlayerReconnectedEvent playerReconnectedEvent = (PlayerReconnectedEvent) event;
        delayedPlayerRemovalService.cancelScheduledRemoval(playerReconnectedEvent.playerKey());
    }

    @Override
    public Class<?> eventType() {
        return PlayerReconnectedEvent.class;
    }
}

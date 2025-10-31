package coffeeshout.global.websocket.infra.handler;

import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.event.player.PlayerReconnectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerReconnectedEventHandler implements EventHandler<PlayerReconnectedEvent> {

    private final DelayedPlayerRemovalService delayedPlayerRemovalService;

    @Override
    public void handle(PlayerReconnectedEvent event) {
        delayedPlayerRemovalService.cancelScheduledRemoval(event.playerKey());
    }

    @Override
    public Class<PlayerReconnectedEvent> eventType() {
        return PlayerReconnectedEvent.class;
    }
}

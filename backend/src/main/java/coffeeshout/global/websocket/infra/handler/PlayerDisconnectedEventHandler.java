package coffeeshout.global.websocket.infra.handler;

import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.event.player.PlayerDisconnectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerDisconnectedEventHandler implements EventHandler<PlayerDisconnectedEvent> {

    private final DelayedPlayerRemovalService delayedPlayerRemovalService;

    @Override
    public void handle(PlayerDisconnectedEvent event) {
        delayedPlayerRemovalService.schedulePlayerRemoval(
                event.playerKey(),
                event.sessionId(),
                event.reason()
        );
    }

    @Override
    public Class<PlayerDisconnectedEvent> eventType() {
        return PlayerDisconnectedEvent.class;
    }
}

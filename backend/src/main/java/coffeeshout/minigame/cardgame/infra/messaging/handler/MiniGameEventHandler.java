package coffeeshout.minigame.cardgame.infra.messaging.handler;

import coffeeshout.minigame.cardgame.domain.event.MiniGameBaseEvent;
import coffeeshout.minigame.cardgame.domain.event.MiniGameEventType;

public interface MiniGameEventHandler<T extends MiniGameBaseEvent> {
    void handle(T event);
    MiniGameEventType getSupportedEventType();
}

package coffeeshout.minigame.infra.messaging.handler;

import coffeeshout.minigame.domain.event.MiniGameBaseEvent;
import coffeeshout.minigame.domain.event.MiniGameEventType;

public interface MiniGameEventHandler<T extends MiniGameBaseEvent> {
    void handle(T event);
    MiniGameEventType getSupportedEventType();
}

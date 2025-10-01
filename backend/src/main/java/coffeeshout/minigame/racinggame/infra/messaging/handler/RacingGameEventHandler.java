package coffeeshout.minigame.racinggame.infra.messaging.handler;

import coffeeshout.minigame.racinggame.domain.event.RacingGameEventType;

public interface RacingGameEventHandler<T> {

    void handle(T event);

    RacingGameEventType getSupportedEventType();
}

package coffeeshout.racinggame.domain.event;

import java.time.Instant;

public interface RacingGameBaseEvent {
    String sessionId();

    String eventId();

    Instant timestamp();

    RacingGameEventType eventType();
}

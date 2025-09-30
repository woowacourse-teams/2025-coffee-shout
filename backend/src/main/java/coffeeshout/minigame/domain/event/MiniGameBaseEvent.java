package coffeeshout.minigame.domain.event;

import java.time.Instant;

public interface MiniGameBaseEvent {
    String getEventId();

    MiniGameEventType getEventType();

    Instant getCreatedAt();
}

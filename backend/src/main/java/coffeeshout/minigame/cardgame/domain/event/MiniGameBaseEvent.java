package coffeeshout.minigame.cardgame.domain.event;

import java.time.LocalDateTime;

public interface MiniGameBaseEvent {
    String getEventId();

    MiniGameEventType getEventType();

    LocalDateTime getCreatedAt();
}

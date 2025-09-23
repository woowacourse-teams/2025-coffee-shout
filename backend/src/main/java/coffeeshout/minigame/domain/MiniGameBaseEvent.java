package coffeeshout.minigame.domain;


import coffeeshout.minigame.domain.event.MiniGameEventType;
import java.time.LocalDateTime;

public interface MiniGameBaseEvent {
    String getEventId();

    MiniGameEventType getEventType();

    LocalDateTime getCreatedAt();
}

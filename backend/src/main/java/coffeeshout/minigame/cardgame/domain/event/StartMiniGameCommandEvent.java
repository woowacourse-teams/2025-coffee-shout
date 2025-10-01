package coffeeshout.minigame.cardgame.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record StartMiniGameCommandEvent(
        String eventId,
        MiniGameEventType eventType,
        LocalDateTime createdAt,
        String joinCode,
        String hostName
) implements MiniGameBaseEvent {

    public static StartMiniGameCommandEvent create(String joinCode, String hostName) {
        final String eventId = UUID.randomUUID().toString();
        return new StartMiniGameCommandEvent(
                eventId,
                MiniGameEventType.START_MINIGAME_COMMAND,
                LocalDateTime.now(),
                joinCode,
                hostName
        );
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public MiniGameEventType getEventType() {
        return eventType;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

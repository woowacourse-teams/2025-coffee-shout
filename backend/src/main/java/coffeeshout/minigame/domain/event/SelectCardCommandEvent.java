package coffeeshout.minigame.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record SelectCardCommandEvent(
        String eventId,
        MiniGameEventType eventType,
        LocalDateTime createdAt,
        String joinCode,
        String playerName,
        Integer cardIndex
) implements MiniGameBaseEvent {

    public static SelectCardCommandEvent create(String joinCode, String playerName, Integer cardIndex) {
        final String eventId = UUID.randomUUID().toString();
        return new SelectCardCommandEvent(
                eventId,
                MiniGameEventType.SELECT_CARD_COMMAND,
                LocalDateTime.now(),
                joinCode,
                playerName,
                cardIndex
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

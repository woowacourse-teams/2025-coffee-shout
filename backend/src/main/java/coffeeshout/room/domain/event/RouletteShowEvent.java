package coffeeshout.room.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record RouletteShowEvent(
        String eventId,
        RoomEventType eventType,
        String joinCode,
        LocalDateTime timestamp
) implements RoomBaseEvent {

    public static RouletteShowEvent create(String joinCode) {
        return new RouletteShowEvent(
                UUID.randomUUID().toString(),
                RoomEventType.ROULETTE_SHOW,
                joinCode,
                LocalDateTime.now()
        );
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public RoomEventType getEventType() {
        return eventType;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

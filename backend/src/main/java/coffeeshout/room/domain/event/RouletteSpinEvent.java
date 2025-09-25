package coffeeshout.room.domain.event;

import coffeeshout.room.domain.player.Winner;
import java.time.LocalDateTime;
import java.util.UUID;

public record RouletteSpinEvent(
        String eventId,
        RoomEventType eventType,
        String joinCode,
        String hostName,
        Winner winner,
        LocalDateTime timestamp
) implements RoomBaseEvent {

    public static RouletteSpinEvent create(String joinCode, String hostName, Winner winner) {
        return new RouletteSpinEvent(
                UUID.randomUUID().toString(),
                RoomEventType.ROULETTE_SPIN,
                joinCode,
                hostName,
                winner,
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

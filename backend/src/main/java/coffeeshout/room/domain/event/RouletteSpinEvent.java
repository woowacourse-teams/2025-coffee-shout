package coffeeshout.room.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record RouletteSpinEvent(
        String eventId,
        RoomEventType eventType,
        String joinCode,
        String hostName,
        LocalDateTime timestamp
) {
    public static RouletteSpinEvent create(final String joinCode, final String hostName) {
        return new RouletteSpinEvent(
                UUID.randomUUID().toString(),
                RoomEventType.ROULETTE_SPIN,
                joinCode,
                hostName,
                LocalDateTime.now()
        );
    }
}

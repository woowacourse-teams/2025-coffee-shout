package coffeeshout.room.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlayerReadyEvent(
        String eventId,
        RoomEventType eventType,
        String joinCode,
        String playerName,
        Boolean isReady,
        LocalDateTime timestamp
) {
    public static PlayerReadyEvent create(String joinCode, String playerName, Boolean isReady) {
        return new PlayerReadyEvent(
                UUID.randomUUID().toString(),
                RoomEventType.PLAYER_READY,
                joinCode,
                playerName,
                isReady,
                LocalDateTime.now()
        );
    }
}

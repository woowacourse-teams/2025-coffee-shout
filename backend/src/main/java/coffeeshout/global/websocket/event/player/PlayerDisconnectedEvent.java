package coffeeshout.global.websocket.event.player;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlayerDisconnectedEvent(
        String eventId,
        PlayerEventType eventType,
        String playerKey,
        String sessionId,
        String reason,
        LocalDateTime timestamp
) implements PlayerBaseEvent {

    public static PlayerDisconnectedEvent create(String playerKey, String sessionId, String reason) {
        return new PlayerDisconnectedEvent(
                UUID.randomUUID().toString(),
                PlayerEventType.PLAYER_DISCONNECTED,
                playerKey,
                sessionId,
                reason,
                LocalDateTime.now()
        );
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public PlayerEventType getEventType() {
        return eventType;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

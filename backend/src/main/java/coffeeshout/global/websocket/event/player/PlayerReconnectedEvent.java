package coffeeshout.global.websocket.event.player;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlayerReconnectedEvent(
        String eventId,
        PlayerEventType eventType,
        String playerKey,
        String sessionId,
        LocalDateTime timestamp
) implements PlayerBaseEvent {

    public static PlayerReconnectedEvent create(String playerKey, String sessionId) {
        return new PlayerReconnectedEvent(
                UUID.randomUUID().toString(),
                PlayerEventType.PLAYER_RECONNECTED,
                playerKey,
                sessionId,
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

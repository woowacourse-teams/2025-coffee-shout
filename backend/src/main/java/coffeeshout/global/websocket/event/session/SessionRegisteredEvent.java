package coffeeshout.global.websocket.event.session;

import java.time.Instant;
import java.util.UUID;

public record SessionRegisteredEvent(
        String eventId,
        SessionEventType eventType,
        String playerKey,
        String sessionId,
        Instant timestamp
) implements SessionBaseEvent {

    public static SessionRegisteredEvent create(String playerKey, String sessionId) {
        return new SessionRegisteredEvent(
                UUID.randomUUID().toString(),
                SessionEventType.SESSION_REGISTERED,
                playerKey,
                sessionId,
                Instant.now()
        );
    }
}

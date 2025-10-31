package coffeeshout.global.websocket.event.session;

import java.time.Instant;
import java.util.UUID;

public record SessionRemovedEvent(
        String eventId,
        SessionEventType eventType,
        String sessionId,
        Instant timestamp
) implements SessionBaseEvent {

    public static SessionRemovedEvent create(String sessionId) {
        return new SessionRemovedEvent(
                UUID.randomUUID().toString(),
                SessionEventType.SESSION_REMOVED,
                sessionId,
                Instant.now()
        );
    }
}

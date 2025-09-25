package coffeeshout.global.websocket.event.session;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionRemovedEvent(
        String eventId,
        SessionEventType eventType,
        String sessionId,
        LocalDateTime timestamp
) implements SessionBaseEvent {

    public static SessionRemovedEvent create(String sessionId) {
        return new SessionRemovedEvent(
                UUID.randomUUID().toString(),
                SessionEventType.SESSION_REMOVED,
                sessionId,
                LocalDateTime.now()
        );
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public SessionEventType getEventType() {
        return eventType;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

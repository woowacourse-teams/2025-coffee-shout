package coffeeshout.global.websocket.event.session;

import java.time.LocalDateTime;

public interface SessionBaseEvent {
    String getEventId();
    SessionEventType getEventType();
    LocalDateTime getTimestamp();
}

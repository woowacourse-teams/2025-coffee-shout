package coffeeshout.room.domain.event;

import java.time.LocalDateTime;

public interface BaseEvent {
    String getEventId();
    RoomEventType getEventType();
    LocalDateTime getTimestamp();
}

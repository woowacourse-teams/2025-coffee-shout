package coffeeshout.room.domain.event;

import java.time.LocalDateTime;

public interface RoomBaseEvent {
    String getEventId();

    RoomEventType getEventType();

    LocalDateTime getTimestamp();
}

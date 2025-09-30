package coffeeshout.room.domain.event;

import java.time.Instant;

public interface RoomBaseEvent {

    String getEventId();

    RoomEventType getEventType();

    Instant getTimeStamp();
}

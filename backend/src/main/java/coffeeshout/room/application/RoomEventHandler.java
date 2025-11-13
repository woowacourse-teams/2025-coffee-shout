package coffeeshout.room.application;

import coffeeshout.room.domain.event.RoomBaseEvent;
import coffeeshout.room.domain.event.RoomEventType;

public interface RoomEventHandler<T extends RoomBaseEvent> {

    void handle(T event);

    RoomEventType getSupportedEventType();
}

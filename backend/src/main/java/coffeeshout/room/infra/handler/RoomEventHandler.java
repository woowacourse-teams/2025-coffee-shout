package coffeeshout.room.infra.handler;

import coffeeshout.room.domain.event.BaseEvent;
import coffeeshout.room.domain.event.RoomEventType;

public interface RoomEventHandler<T extends BaseEvent> {
    
    void handle(T event);
    
    RoomEventType getSupportedEventType();
}

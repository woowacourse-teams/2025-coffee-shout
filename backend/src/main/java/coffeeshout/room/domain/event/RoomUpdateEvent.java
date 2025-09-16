package coffeeshout.room.domain.event;

import coffeeshout.room.domain.Room;

public record RoomUpdateEvent(Room room) {
}
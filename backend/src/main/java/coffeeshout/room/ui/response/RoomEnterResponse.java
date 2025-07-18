package coffeeshout.room.ui.response;

import coffeeshout.room.domain.Room;

public record RoomEnterResponse(Long roomId) {

    public static RoomEnterResponse from(Room room) {
        return new RoomEnterResponse(room.getId());
    }
}

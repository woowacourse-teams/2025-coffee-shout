package coffeeshout.room.ui.response;

import coffeeshout.room.domain.Room;

public record RoomCreateResponse(Long roomId) {

    public static RoomCreateResponse from(Room room) {
        return new RoomCreateResponse(
                room.getId()
        );
    }
}

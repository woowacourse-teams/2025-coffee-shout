package coffeeshout.room.ui.response;

import coffeeshout.room.domain.Room;

public record RoomCreateResponse(
        String joinCode
) {

    public static RoomCreateResponse from(Room room) {
        return new RoomCreateResponse(
                room.getJoinCode().value()
        );
    }
}

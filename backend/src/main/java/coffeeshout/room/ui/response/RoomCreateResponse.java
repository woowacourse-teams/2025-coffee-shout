package coffeeshout.room.ui.response;

import coffeeshout.room.domain.RouletteRoom;

public record RoomCreateResponse(Long roomId) {

    public static RoomCreateResponse from(RouletteRoom room) {
        return new RoomCreateResponse(
                room.getId()
        );
    }
}

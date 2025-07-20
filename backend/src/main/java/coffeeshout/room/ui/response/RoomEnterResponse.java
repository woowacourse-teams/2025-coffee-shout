package coffeeshout.room.ui.response;

import coffeeshout.room.domain.RouletteRoom;

public record RoomEnterResponse(Long roomId) {

    public static RoomEnterResponse from(RouletteRoom room) {
        return new RoomEnterResponse(room.getId());
    }
}

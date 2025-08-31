package coffeeshout.room.ui.response;

import coffeeshout.room.domain.Room;

public record RoomEnterResponse(
        String joinCode) {

    public static RoomEnterResponse from(Room room) {
        return new RoomEnterResponse(room.getJoinCode().value());
    }
}

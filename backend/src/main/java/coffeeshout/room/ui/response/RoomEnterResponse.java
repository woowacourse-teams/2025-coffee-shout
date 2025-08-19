package coffeeshout.room.ui.response;

import coffeeshout.generator.WebsocketMessage;
import coffeeshout.room.domain.Room;

@WebsocketMessage
public record RoomEnterResponse(
        String joinCode) {

    public static RoomEnterResponse from(Room room) {
        return new RoomEnterResponse(room.getJoinCode().value());
    }
}

package coffeeshout.room.ui.response;

import coffeeshout.room.domain.Room;
import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record RoomEnterResponse(
        String joinCode) {

    public static RoomEnterResponse from(Room room) {
        return new RoomEnterResponse(room.getJoinCode().value());
    }
}

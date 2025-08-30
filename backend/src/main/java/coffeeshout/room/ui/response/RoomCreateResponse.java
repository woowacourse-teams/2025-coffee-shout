package coffeeshout.room.ui.response;

import coffeeshout.room.domain.Room;
import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record RoomCreateResponse(
        String joinCode
) {

    public static RoomCreateResponse from(Room room) {
        return new RoomCreateResponse(
                room.getJoinCode().value()
        );
    }
}

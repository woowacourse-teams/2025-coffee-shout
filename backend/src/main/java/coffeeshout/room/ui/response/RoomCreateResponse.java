package coffeeshout.room.ui.response;

import coffeeshout.generator.WebsocketMessage;
import coffeeshout.room.domain.Room;

@WebsocketMessage
public record RoomCreateResponse(
        String joinCode
) {

    public static RoomCreateResponse from(Room room) {
        return new RoomCreateResponse(
                room.getJoinCode().value()
        );
    }
}

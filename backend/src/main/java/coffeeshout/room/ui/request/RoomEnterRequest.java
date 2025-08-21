package coffeeshout.room.ui.request;

import coffeeshout.generator.WebsocketMessage;

@WebsocketMessage
public record RoomEnterRequest(
        String joinCode,
        String guestName,
        Long menuId
) {
}

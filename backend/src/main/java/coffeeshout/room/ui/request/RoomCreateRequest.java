package coffeeshout.room.ui.request;

import coffeeshout.generator.WebsocketMessage;

@WebsocketMessage
public record RoomCreateRequest(
        String hostName,
        Long menuId
) {
}

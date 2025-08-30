package coffeeshout.room.ui.request;

import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record RoomEnterRequest(
        String joinCode,
        String guestName,
        Long menuId
) {
}

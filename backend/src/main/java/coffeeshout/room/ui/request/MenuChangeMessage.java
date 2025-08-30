package coffeeshout.room.ui.request;

import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record MenuChangeMessage(
        String playerName,
        Long menuId
) {
}

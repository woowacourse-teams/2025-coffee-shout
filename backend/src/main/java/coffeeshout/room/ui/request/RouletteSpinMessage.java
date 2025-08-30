package coffeeshout.room.ui.request;

import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record RouletteSpinMessage(
        String hostName
) {
}

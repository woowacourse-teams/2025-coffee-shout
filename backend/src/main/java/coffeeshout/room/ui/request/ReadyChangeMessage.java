package coffeeshout.room.ui.request;

import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record ReadyChangeMessage(
        String joinCode,
        String playerName,
        Boolean isReady
) {
}

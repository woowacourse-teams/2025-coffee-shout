package coffeeshout.room.ui.request;

import coffeeshout.generator.WebsocketMessage;

@WebsocketMessage
public record ReadyChangeMessage(
        String joinCode,
        String playerName,
        Boolean isReady
) {
}

package coffeeshout.room.ui.request;

import coffeeshout.generator.WebsocketMessage;

@WebsocketMessage
public record RouletteSpinMessage(
        String hostName
) {
}

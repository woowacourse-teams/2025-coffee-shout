package coffeeshout.room.ui.request;


import coffeeshout.generator.WebsocketMessage;
import coffeeshout.minigame.domain.MiniGameType;
import java.util.List;

@WebsocketMessage
public record MiniGameSelectMessage(
        String hostName,
        List<MiniGameType> miniGameTypes
) {
}

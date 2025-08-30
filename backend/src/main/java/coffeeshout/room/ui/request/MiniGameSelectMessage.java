package coffeeshout.room.ui.request;


import coffeeshout.minigame.domain.MiniGameType;
import generator.annotaions.WebSocketMessage;
import java.util.List;

@WebSocketMessage
public record MiniGameSelectMessage(
        String hostName,
        List<MiniGameType> miniGameTypes
) {
}

package coffeeshout.room.ui.response;


import coffeeshout.minigame.domain.MiniGameType;
import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record MiniGameResponse(
        MiniGameType miniGameType
) {

    public static MiniGameResponse from(MiniGameType miniGameType) {
        return new MiniGameResponse(miniGameType);
    }
}

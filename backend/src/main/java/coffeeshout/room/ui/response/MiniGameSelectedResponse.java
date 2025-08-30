package coffeeshout.room.ui.response;

import coffeeshout.minigame.domain.MiniGameType;
import generator.annotaions.WebSocketMessage;

@WebSocketMessage
public record MiniGameSelectedResponse(MiniGameType miniGameType) {

    public static MiniGameSelectedResponse from(MiniGameType miniGameType) {
        return new MiniGameSelectedResponse(miniGameType);
    }
}

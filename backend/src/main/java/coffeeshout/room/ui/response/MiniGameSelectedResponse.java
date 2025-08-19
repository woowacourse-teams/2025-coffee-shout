package coffeeshout.room.ui.response;

import coffeeshout.generator.WebsocketMessage;
import coffeeshout.minigame.domain.MiniGameType;

@WebsocketMessage
public record MiniGameSelectedResponse(MiniGameType miniGameType) {

    public static MiniGameSelectedResponse from(MiniGameType miniGameType) {
        return new MiniGameSelectedResponse(miniGameType);
    }
}

package coffeeshout.room.ui.response;


import coffeeshout.generator.WebsocketMessage;
import coffeeshout.minigame.domain.MiniGameType;

@WebsocketMessage
public record MiniGameResponse(
        MiniGameType miniGameType
) {

    public static MiniGameResponse from(MiniGameType miniGameType) {
        return new MiniGameResponse(miniGameType);
    }
}

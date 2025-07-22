package coffeeshout.room.ui.response;

import coffeeshout.room.domain.MiniGameType;

public record MiniGameResponse(
        MiniGameType miniGameType
) {

    public static MiniGameResponse from(MiniGameType miniGameType) {
        return new MiniGameResponse(miniGameType);
    }
}

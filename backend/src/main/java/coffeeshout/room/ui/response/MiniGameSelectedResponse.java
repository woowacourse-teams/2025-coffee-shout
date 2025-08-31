package coffeeshout.room.ui.response;

import coffeeshout.minigame.domain.MiniGameType;

public record MiniGameSelectedResponse(MiniGameType miniGameType) {

    public static MiniGameSelectedResponse from(MiniGameType miniGameType) {
        return new MiniGameSelectedResponse(miniGameType);
    }
}

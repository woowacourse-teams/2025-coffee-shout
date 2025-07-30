package coffeeshout.room.ui.request;


import coffeeshout.minigame.domain.MiniGameType;

public record MiniGameSelectMessage(
        String hostName,
        MiniGameType miniGameType
) {
}

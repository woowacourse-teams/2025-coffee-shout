package coffeeshout.room.ui.request;


import coffeeshout.minigame.cardgame.domain.MiniGameType;
import java.util.List;

public record MiniGameSelectMessage(
        String hostName,
        List<MiniGameType> miniGameTypes
) {
}

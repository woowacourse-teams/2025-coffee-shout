package coffeeshout.room.ui.response;

import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerType;
import java.util.Random;

public record WinnerResponse(
        String playerName,
        MenuResponse menuResponse,
        PlayerType playerType,
        Boolean isReady,
        Integer colorIndex,
        Integer randomAngle
) {

    private static final Random RANDOM = new Random();
    private static final int PIE_SEGMENT = 10;

    public static WinnerResponse from(Player player) {
        return new WinnerResponse(
                player.getName().value(),
                MenuResponse.from(player.getMenu()),
                player.getPlayerType(),
                player.getIsReady(),
                player.getColorIndex(),
                RANDOM.nextInt(PIE_SEGMENT)
        );
    }
}

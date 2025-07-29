package coffeeshout.room.ui.response;

import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerType;

public record PlayerResponse(
        String playerName,
        MenuResponse menuResponse,
        PlayerType playerType
) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getName().value(),
                MenuResponse.from(player.getMenu()),
                player.getPlayerType()
        );
    }
}

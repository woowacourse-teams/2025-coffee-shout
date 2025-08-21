package coffeeshout.room.ui.response;

import coffeeshout.generator.WebsocketMessage;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerType;

@WebsocketMessage
public record PlayerResponse(
        String playerName,
        MenuResponse menuResponse,
        PlayerType playerType,
        Boolean isReady,
        Integer colorIndex
) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getName().value(),
                MenuResponse.from(player.getMenu()),
                player.getPlayerType(),
                player.getIsReady(),
                player.getColorIndex()
        );
    }
}

package coffeeshout.room.ui.response;

import coffeeshout.room.domain.player.Player;

public record PlayerResponse(
        String playerName,
        MenuResponse menuResponse
) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getName().value(),
                MenuResponse.from(player.getMenu())
        );
    }
}

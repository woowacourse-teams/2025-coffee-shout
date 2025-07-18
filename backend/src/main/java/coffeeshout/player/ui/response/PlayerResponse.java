package coffeeshout.player.ui.response;

import coffeeshout.player.domain.Player;

public record PlayerResponse(
        String playerName,
        MenuResponse menuResponse
) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getName(),
                MenuResponse.from(player.getMenu())
        );
    }
}

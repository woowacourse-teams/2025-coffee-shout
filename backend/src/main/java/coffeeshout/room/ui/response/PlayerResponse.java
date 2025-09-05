package coffeeshout.room.ui.response;

import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerType;

public record PlayerResponse(
        String playerName,
        PlayerMenuResponse menuResponse,
        PlayerType playerType,
        Boolean isReady,
        Integer colorIndex
) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getName().value(),
                PlayerMenuResponse.from(player),
                player.getPlayerType(),
                player.getIsReady(),
                player.getColorIndex()
        );
    }

    public record PlayerMenuResponse(
            Long id,
            String name,
            String temperature,
            String categoryImageUrl
    ) {

        public static PlayerMenuResponse from(Player player) {
            return new PlayerMenuResponse(
                    player.getMenu().getId(),
                    player.getMenu().getName(),
                    player.getMenuTemperature().name(),
                    player.getMenu().getCategoryImageUrl()
            );
        }
    }
}

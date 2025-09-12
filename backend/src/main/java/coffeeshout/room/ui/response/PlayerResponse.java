package coffeeshout.room.ui.response;

import coffeeshout.room.domain.menu.Menu;
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
            Menu menu = player.getSelectedMenu().menu();
            Long menuId;

            try {
                menuId = menu.getId();
            } catch (IllegalStateException e) {
                // CustomMenu의 경우 id가 없으므로 null 반환
                menuId = null;
            }

            return new PlayerMenuResponse(
                    menuId,
                    menu.getName(),
                    player.getSelectedMenu().menuTemperature().name(),
                    menu.getCategoryImageUrl()
            );
        }
    }
}

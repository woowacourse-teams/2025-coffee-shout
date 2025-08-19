package coffeeshout.room.ui.response;

import coffeeshout.generator.WebsocketMessage;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.MenuType;

@WebsocketMessage
public record MenuResponse(
        Long id,
        String name,
        MenuType menuType
) {

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(menu.getId(), menu.getName(), menu.getMenuType());
    }
}

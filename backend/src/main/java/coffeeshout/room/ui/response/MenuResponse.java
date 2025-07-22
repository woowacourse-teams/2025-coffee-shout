package coffeeshout.room.ui.response;

import coffeeshout.room.domain.player.Menu;

public record MenuResponse(
        String name,
        String image
) {

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(menu.getName(), menu.getImage());
    }
}

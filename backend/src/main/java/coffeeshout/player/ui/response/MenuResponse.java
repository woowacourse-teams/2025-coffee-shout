package coffeeshout.player.ui.response;

import coffeeshout.player.domain.Menu;

public record MenuResponse(
        String name,
        String image
) {

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(menu.getName(), menu.getImage());
    }
}

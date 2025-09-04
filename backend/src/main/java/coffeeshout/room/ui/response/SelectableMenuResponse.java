package coffeeshout.room.ui.response;

import coffeeshout.room.domain.menu.Menu;
import java.util.List;

public record SelectableMenuResponse(Long id, String name, String temperatureAvailability) {

    public static SelectableMenuResponse from(Menu menu) {
        return new SelectableMenuResponse(menu.getId(), menu.getName(), menu.getTemperatureAvailability().name());
    }

    public static List<SelectableMenuResponse> from(List<Menu> menu) {
        return menu.stream()
                .map(SelectableMenuResponse::from)
                .toList();
    }
}

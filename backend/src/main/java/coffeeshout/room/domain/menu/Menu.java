package coffeeshout.room.domain.menu;

import lombok.Getter;

@Getter
public class Menu {

    private Long id;
    private final String name;
    private final Long menuCategoryId;
    private final TemperatureAvailability temperatureAvailability;

    public Menu(
            Long id,
            String name,
            Long menuCategoryId,
            TemperatureAvailability temperatureAvailability
    ) {
        this.id = id;
        this.name = name;
        this.menuCategoryId = menuCategoryId;
        this.temperatureAvailability = temperatureAvailability;
    }

    public Menu(
            String name,
            Long menuCategoryId,
            TemperatureAvailability temperatureAvailability
    ) {
        this.name = name;
        this.menuCategoryId = menuCategoryId;
        this.temperatureAvailability = temperatureAvailability;
    }

    public void setId(long id) {
        this.id = id;
    }
}

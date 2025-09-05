package coffeeshout.room.domain.menu;

import lombok.Getter;

@Getter
public class Menu {

    private Long id;
    private final String name;
    private final MenuCategory menuCategory;
    private final TemperatureAvailability temperatureAvailability;

    public Menu(
            Long id,
            String name,
            MenuCategory menuCategory,
            TemperatureAvailability temperatureAvailability
    ) {
        this.id = id;
        this.name = name;
        this.menuCategory = menuCategory;
        this.temperatureAvailability = temperatureAvailability;
    }

    public Menu(
            String name,
            MenuCategory menuCategory,
            TemperatureAvailability temperatureAvailability
    ) {
        this.name = name;
        this.menuCategory = menuCategory;
        this.temperatureAvailability = temperatureAvailability;
    }

    public void setId(long id) {
        this.id = id;
    }
}

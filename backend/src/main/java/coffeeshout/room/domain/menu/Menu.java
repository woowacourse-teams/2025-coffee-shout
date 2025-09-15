package coffeeshout.room.domain.menu;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public abstract class Menu {

    private String name;
    private TemperatureAvailability temperatureAvailability;

    protected Menu(String name, TemperatureAvailability temperatureAvailability) {
        this.name = name;
        this.temperatureAvailability = temperatureAvailability;
    }

    public abstract String getCategoryImageUrl();

    public abstract Long getId();
}

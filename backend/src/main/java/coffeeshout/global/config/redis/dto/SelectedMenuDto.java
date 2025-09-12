package coffeeshout.global.config.redis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SelectedMenuDto {
    
    private final MenuDto menu;
    private final String menuTemperature;

    @JsonCreator
    public SelectedMenuDto(
            @JsonProperty("menu") MenuDto menu,
            @JsonProperty("menuTemperature") String menuTemperature
    ) {
        this.menu = menu;
        this.menuTemperature = menuTemperature;
    }
}

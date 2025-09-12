package coffeeshout.global.config.redis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class MenuDto {
    
    private final String type; // "PROVIDED" or "CUSTOM"
    private final Long id; // ProvidedMenu의 경우만
    private final String name;
    private final String categoryImageUrl;
    private final String temperatureAvailability;

    @JsonCreator
    public MenuDto(
            @JsonProperty("type") String type,
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("categoryImageUrl") String categoryImageUrl,
            @JsonProperty("temperatureAvailability") String temperatureAvailability
    ) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.categoryImageUrl = categoryImageUrl;
        this.temperatureAvailability = temperatureAvailability;
    }
}

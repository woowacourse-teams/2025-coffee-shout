package coffeeshout.global.config.redis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CardDto {
    
    private final String type;
    private final int value;
    private final String className;

    @JsonCreator
    public CardDto(
            @JsonProperty("type") String type,
            @JsonProperty("value") int value,
            @JsonProperty("className") String className
    ) {
        this.type = type;
        this.value = value;
        this.className = className;
    }
}

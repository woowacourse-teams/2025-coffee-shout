package coffeeshout.global.config.redis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PlayerProbabilityDto {
    
    private final String playerName;
    private final Integer probabilityValue;

    @JsonCreator
    public PlayerProbabilityDto(
            @JsonProperty("playerName") String playerName,
            @JsonProperty("probabilityValue") Integer probabilityValue
    ) {
        this.playerName = playerName;
        this.probabilityValue = probabilityValue;
    }
}

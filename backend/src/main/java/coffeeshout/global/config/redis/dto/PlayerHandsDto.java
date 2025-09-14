package coffeeshout.global.config.redis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
public class PlayerHandsDto {
    
    private final Map<String, CardHandDto> playerHands; // playerName -> CardHandDto

    @JsonCreator
    public PlayerHandsDto(@JsonProperty("playerHands") Map<String, CardHandDto> playerHands) {
        this.playerHands = playerHands;
    }
}

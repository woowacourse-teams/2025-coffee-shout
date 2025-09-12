package coffeeshout.global.config.redis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PlayerScoreDto {
    
    private final String playerName;
    private final Integer score;

    @JsonCreator
    public PlayerScoreDto(
            @JsonProperty("playerName") String playerName,
            @JsonProperty("score") Integer score
    ) {
        this.playerName = playerName;
        this.score = score;
    }
}

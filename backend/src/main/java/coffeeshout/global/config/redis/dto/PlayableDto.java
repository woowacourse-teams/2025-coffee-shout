package coffeeshout.global.config.redis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class PlayableDto {
    
    private final String miniGameType;
    private final String gameType; // "CARD_GAME" 등으로 구분
    private final List<PlayerScoreDto> scores;
    private final Map<String, Object> gameState; // 게임별 상태 저장용
    private final PlayerHandsDto playerHands; // CardGame의 playerHands 정보

    @JsonCreator
    public PlayableDto(
            @JsonProperty("miniGameType") String miniGameType,
            @JsonProperty("gameType") String gameType,
            @JsonProperty("scores") List<PlayerScoreDto> scores,
            @JsonProperty("gameState") Map<String, Object> gameState,
            @JsonProperty("playerHands") PlayerHandsDto playerHands
    ) {
        this.miniGameType = miniGameType;
        this.gameType = gameType;
        this.scores = scores;
        this.gameState = gameState;
        this.playerHands = playerHands;
    }
}

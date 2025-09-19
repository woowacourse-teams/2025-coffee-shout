package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.player.Player;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MiniGameServiceManager {

    private final Map<MiniGameType, MiniGameService> gameServices;

    public MiniGameServiceManager(CardGameService cardGameService) {
        this.gameServices = new EnumMap<>(MiniGameType.class);
        this.gameServices.put(MiniGameType.CARD_GAME, cardGameService);
    }

    public void startGame(MiniGameType gameType, String joinCode, List<Player> players) {
        getMiniGameService(gameType).start(joinCode, players);
    }

    public Map<Player, MiniGameScore> getMiniGameScores(String joinCode, MiniGameType gameType) {
        return getMiniGameService(gameType).getMiniGameScores(joinCode);
    }

    public MiniGameResult getMiniGameRanks(String joinCode, MiniGameType gameType) {
        return getMiniGameService(gameType).getMiniGameRanks(joinCode);
    }

    private MiniGameService getMiniGameService(MiniGameType gameType) {
        final MiniGameService gameService = gameServices.get(gameType);
        if (gameService == null) {
            throw new IllegalArgumentException("지원하지 않는 게임 타입입니다: " + gameType);
        }
        return gameService;
    }
}

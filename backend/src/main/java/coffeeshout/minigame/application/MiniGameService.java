package coffeeshout.minigame.application;


import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.room.domain.player.Player;
import java.util.List;
import java.util.Map;

public interface MiniGameService {

    void start(String joinCode, List<Player> players);

    Map<Player, MiniGameScore> getMiniGameScores(String joinCode);

    MiniGameResult getMiniGameRanks(String joinCode);
}

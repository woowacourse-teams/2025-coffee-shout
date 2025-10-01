package coffeeshout.room.domain;

import coffeeshout.minigame.cardgame.domain.MiniGameResult;
import coffeeshout.minigame.cardgame.domain.MiniGameScore;
import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.room.domain.player.Player;
import java.util.List;
import java.util.Map;

public interface Playable {

    MiniGameResult getResult();

    Map<Player, MiniGameScore> getScores();

    MiniGameType getMiniGameType();

    void startGame(List<Player> players);
}

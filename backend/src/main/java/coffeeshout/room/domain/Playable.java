package coffeeshout.room.domain;

import coffeeshout.minigame.MiniGameResult;
import coffeeshout.minigame.MiniGameScore;
import coffeeshout.minigame.MiniGameType;
import coffeeshout.room.domain.player.Player;
import java.util.List;
import java.util.Map;

public interface Playable {

    MiniGameResult getResult();

    Map<Player, MiniGameScore> getScores();

    MiniGameType getMiniGameType();

    void startGame(List<Player> players);
}

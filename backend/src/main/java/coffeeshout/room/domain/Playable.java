package coffeeshout.room.domain;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.player.domain.Player;
import java.util.List;

public interface Playable {

    void startGame(List<Player> players);

    MiniGameResult getResult();

    MiniGameType getMiniGameType();
}

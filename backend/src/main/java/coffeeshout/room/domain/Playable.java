package coffeeshout.room.domain;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.player.Player;
import java.util.List;

public interface Playable {

    MiniGameResult getResult();

    MiniGameType getMiniGameType();

    void startGame(List<Player> players);
}

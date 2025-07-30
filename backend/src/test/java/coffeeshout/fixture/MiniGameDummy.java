package coffeeshout.fixture;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.player.Player;
import java.util.List;

public class MiniGameDummy implements Playable {

    @Override
    public MiniGameResult getResult() {
        return null;
    }

    @Override
    public MiniGameType getMiniGameType() {
        return null;
    }

    @Override
    public void startGame(List<Player> players) {
    }
}

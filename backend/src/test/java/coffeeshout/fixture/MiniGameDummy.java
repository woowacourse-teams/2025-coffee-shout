package coffeeshout.fixture;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.MiniGameType;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.player.Players;

public class MiniGameDummy implements Playable {

    @Override
    public void start() {

    }

    @Override
    public MiniGameResult getResult() {
        return null;
    }

    @Override
    public MiniGameType getMiniGameType() {
        return null;
    }

    @Override
    public void assignPlayers(Players players) {

    }
}

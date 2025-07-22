package coffeeshout.fixture;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.MiniGameType;
import coffeeshout.room.domain.Playable;

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
}

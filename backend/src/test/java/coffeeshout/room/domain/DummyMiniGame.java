package coffeeshout.room.domain;

import coffeeshout.minigame.domain.MiniGameResult;

public class DummyMiniGame implements Playable {

    @Override
    public void start() {

    }

    @Override
    public MiniGameResult getResult() {
        return null;
    }
}

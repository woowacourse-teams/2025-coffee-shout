package coffeeshout.room.domain;

import coffeeshout.minigame.domain.MiniGameResult;

public interface Playable {

    void start();

    MiniGameResult getResult();
}

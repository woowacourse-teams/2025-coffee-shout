package coffeeshout.minigame.cardgame.application;

import coffeeshout.minigame.cardgame.domain.MiniGameType;

public interface MiniGameService {

    void start(String joinCode, String hostName);

    MiniGameType getMiniGameType();
}

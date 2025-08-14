package coffeeshout.minigame.application;

import coffeeshout.room.domain.Playable;

public interface MiniGameService {

    void start(Playable playable, String joinCode);
}

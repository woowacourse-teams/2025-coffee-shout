package coffeeshout.room.domain;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.player.Players;

public interface Playable {

    void start();

    MiniGameResult getResult();

    MiniGameType getMiniGameType();

    void assignPlayers(Players players);
}

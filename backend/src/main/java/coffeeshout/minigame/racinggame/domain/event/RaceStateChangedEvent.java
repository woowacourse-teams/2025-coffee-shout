package coffeeshout.minigame.racinggame.domain.event;

import coffeeshout.minigame.racinggame.domain.RacingGame;

public record RaceStateChangedEvent(String joinCode, String state) {

    public static RaceStateChangedEvent of(RacingGame racingGame, String joinCode) {
        return new RaceStateChangedEvent(joinCode, racingGame.getState().name());
    }
}

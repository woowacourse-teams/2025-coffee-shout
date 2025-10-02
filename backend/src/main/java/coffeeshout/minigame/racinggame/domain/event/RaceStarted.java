package coffeeshout.minigame.racinggame.domain.event;

import coffeeshout.minigame.racinggame.domain.RacingGame;

public record RaceStarted(String joinCode, String state) {

    public static RaceStarted of(RacingGame racingGame, String joinCode) {
        return new RaceStarted(joinCode, racingGame.getState().name());
    }
}

package coffeeshout.minigame.racinggame.domain.event;

import coffeeshout.minigame.racinggame.domain.RacingGame;

public record RaceFinished(String state, String joinCode) {

    public static RaceFinished of(RacingGame racingGame, String joinCode) {
        return new RaceFinished(racingGame.getState().name(), joinCode);
    }
}

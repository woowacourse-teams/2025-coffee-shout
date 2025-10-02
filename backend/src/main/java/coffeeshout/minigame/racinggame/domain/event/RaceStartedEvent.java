package coffeeshout.minigame.racinggame.domain.event;

import coffeeshout.minigame.racinggame.domain.RacingGame;

public record RaceStartedEvent(String joinCode, String state) {

    public static RaceStartedEvent of(RacingGame racingGame, String joinCode) {
        return new RaceStartedEvent(joinCode, racingGame.getState().name());
    }
}

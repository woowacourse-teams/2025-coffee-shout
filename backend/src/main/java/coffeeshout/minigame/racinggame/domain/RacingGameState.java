package coffeeshout.minigame.racinggame.domain;

import lombok.Getter;

@Getter
public enum RacingGameState {
    DESCRIPTION(4000),
    PREPARE(2000),
    PLAYING(30000),
    DONE(0),
    ;

    final long duration;

    RacingGameState(int duration) {
        this.duration = duration;
    }
}

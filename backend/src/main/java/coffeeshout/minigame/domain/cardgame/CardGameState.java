package coffeeshout.minigame.domain.cardgame;

import java.time.Duration;
import lombok.Getter;

@Getter
public enum CardGameState {
    READY(0),
    LOADING(3000),
    DESCRIPTION(1500),
    PLAYING(10250),
    SCORE_BOARD(1500),
    DONE(0),
    ;

    private final int duration;

    CardGameState(int duration) {
        this.duration = duration;
    }

    public Duration getDurationMillis() {
        return Duration.ofMillis(duration);
    }
}

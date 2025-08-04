package coffeeshout.minigame.domain.cardgame;

import java.time.Duration;
import lombok.Getter;

@Getter
public enum CardGameState {
    READY(0),
    LOADING(3000),
    PLAYING(10000),
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

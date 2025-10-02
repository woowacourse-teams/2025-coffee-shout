package coffeeshout.minigame.racinggame.domain;

import coffeeshout.minigame.cardgame.domain.MiniGameScore;
import java.time.Instant;

public class RacingGameScore extends MiniGameScore {

    private final Instant finishTime;

    public RacingGameScore(Instant finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public int getValue() {
        return Math.toIntExact(finishTime.toEpochMilli());
    }
}

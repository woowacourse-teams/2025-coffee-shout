package coffeeshout.minigame.racinggame.domain;

import coffeeshout.minigame.cardgame.domain.MiniGameScore;

public class RacingGameScore extends MiniGameScore {

    private final int ranking;

    public RacingGameScore(int ranking) {
        this.ranking = ranking;
    }

    @Override
    public int getValue() {
        return ranking;
    }
}

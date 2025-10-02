package coffeeshout.minigame.racinggame.domain;

import coffeeshout.minigame.cardgame.domain.MiniGameScore;

/**
 * 레이싱 게임 점수
 * 순위가 높을수록 점수가 높음 (1등이 가장 높은 점수)
 */
public class RacingGameScore extends MiniGameScore {

    private final int ranking;

    public RacingGameScore(int ranking) {
        this.ranking = ranking;
    }

    @Override
    public int getValue() {
        // 순위가 낮을수록 높은 점수 (1등 = 가장 높은 점수)
        return 1000 - ranking;
    }
}

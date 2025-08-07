package coffeeshout.room.domain.roulette;

import static org.springframework.util.Assert.isTrue;

import coffeeshout.minigame.domain.MiniGameResultType;

/*
    등수별 확률 조정 정도를 계산하는 클래스
 */
public class ProbabilityCalculator {

    private static final double ADJUSTMENT_WEIGHT = 0.7;

    private final Integer playerCount;
    private final Integer roundCount;

    public ProbabilityCalculator(Integer playerCount, Integer roundCount) {
        validate(playerCount, roundCount);
        this.playerCount = playerCount;
        this.roundCount = roundCount;
    }

    public Probability calculateAdjustProbability(RankNormalization rankNormalization, int rank,
                                                  Probability probability) {
        final int maxRank = rankNormalization.getMaxRank();
        final MiniGameResultType resultType = MiniGameResultType.of(maxRank, rank);
        final Probability adjustProbability = resultType.adjustProbability(
                countAdjustableRanks(maxRank),
                relativeRank(maxRank, rank),
                computeAdjustmentStep(maxRank));

        return probability.adjust(resultType, adjustProbability.divide(rankNormalization.getTieSize(rank)));
    }

    private int relativeRank(int maxRank, int rank) {
        if (rank <= countAdjustableRanks(maxRank)) {
            return rank;
        }
        return maxRank - rank + 1;
    }

    private Probability computeAdjustmentStep(int maxRank) {
        final Probability maxAdjustment = computeInitialProbability().divide(roundCount);
        return maxAdjustment.divide(countAdjustableRanks(maxRank)).multiple(ADJUSTMENT_WEIGHT);
    }

    private Probability computeInitialProbability() {
        return Probability.TOTAL.divide(playerCount);
    }

    private int countAdjustableRanks(int maxRank) {
        return maxRank / 2;
    }

    private void validate(Integer playerCount, Integer roundCount) {
        isTrue(playerCount >= 2, "플레이어는 2명 이상이어야 합니다.");
        isTrue(roundCount > 0, "라운드 수는 양수여야 합니다.");
    }
}

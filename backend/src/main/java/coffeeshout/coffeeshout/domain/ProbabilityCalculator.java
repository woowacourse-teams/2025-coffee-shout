package coffeeshout.coffeeshout.domain;

import static org.springframework.util.Assert.state;

/*
    등수별 확률 조정 정도를 계산하는 클래스
 */
public class ProbabilityCalculator {

    private final Integer playerCount;
    private final Integer roundCount;

    public ProbabilityCalculator(Integer playerCount, Integer roundCount) {
        validate(playerCount, roundCount);
        this.playerCount = playerCount;
        this.roundCount = roundCount;
    }

    public static Probability computeInitialProbability(int playerCount) {
        return Probability.CERTAIN.divide(playerCount);
    }

    public Probability calculateAdjustProbability(int playerCount, int rank) {
        final MiniGameResultType resultType = MiniGameResultType.of(playerCount, rank);
        return resultType.adjustProbability(countAdjustableRanks(), relativeRank(rank), computeAdjustmentStep());
    }

    private int relativeRank(int rank) {
        if (rank <= countAdjustableRanks()) {
            return rank;
        }
        return playerCount - rank + 1;
    }

    private Probability computeAdjustmentStep() {
        final Probability maxAdjustment = computeInitialProbability(playerCount).divide(roundCount);
        return maxAdjustment.divide(countAdjustableRanks());
    }

    private int countAdjustableRanks() {
        return playerCount / 2;
    }

    private void validate(Integer playerCount, Integer roundCount) {
        state(playerCount > 2, "플레이어는 2명 이상이어야 합니다.");
        state(roundCount > 0, "라운드 수는 양수여야 합니다.");
    }
}

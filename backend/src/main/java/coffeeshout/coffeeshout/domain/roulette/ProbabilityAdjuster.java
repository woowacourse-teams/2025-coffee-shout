package coffeeshout.coffeeshout.domain.roulette;

import static org.springframework.util.Assert.state;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ProbabilityAdjuster {

    private final Integer playerCount;
    private final Integer roundCount;
    private final Map<Integer, Probability> adjustedProbabilities;

    public ProbabilityAdjuster(Integer playerCount, Integer roundCount) {
        validate(playerCount, roundCount);
        this.playerCount = playerCount;
        this.roundCount = roundCount;
        adjustProbabilities = initialAdjustProbability();
        adjustProbabilities();
    }

    private Map<Integer, Probability> initialAdjustProbabilities() {
        Map<Integer, Probability> probability = HashMap.newHashMap(playerCount);
        IntStream.rangeClosed(1, playerCount).forEach(rank -> probability.put(rank, new Probability(0)));
        return probability;
    }

    public Probability getAdjustProbability(int rank) {
        state(adjustedProbabilities.containsKey(rank), "해당 순위는 존재하지 않습니다. rank=" + rank);
        return adjustedProbabilities.get(rank);
    }

    private void adjustProbabilities() {
        adjustTopPlayerProbabilities();
        adjustBottomPlayerProbabilities();
    }

    private void adjustBottomPlayerProbabilities() {
        Probability current = getMaxAdjustableRangePerRound();
        final int startRank = playerCount;
        final int endRank = playerCount - countEffectivePlayer();
        for (int rank = startRank; rank >= endRank; rank--) {
            adjustedProbabilities.put(rank, current);
            current = current.minus(computeAdjustProbabilityStep());
        }
    }

    private void adjustTopPlayerProbabilities() {
        Probability invertCurrent = getMaxAdjustableRangePerRound().invert();
        final int startRank = 1;
        final int endRank = countEffectivePlayer();
        for (int rank = startRank; rank <= endRank; rank++) {
            adjustedProbabilities.put(rank, invertCurrent);
            invertCurrent = invertCurrent.plus(computeAdjustProbabilityStep());
        }
    }

    private Probability getMaxAdjustableRangePerRound() {
        Probability initialProbability = initialProbability(playerCount);
        return initialProbability.divide(roundCount);
    }

    private Probability computeAdjustProbabilityStep() {
        return getMaxAdjustableRangePerRound().divide(countEffectivePlayer());
    }

    public static Probability initialProbability(int playerCount) {
        return new Probability(10000 / playerCount);
    }

    private int countEffectivePlayer() {
        return playerCount / 2;
    }

    private void validate(Integer playerCount, Integer roundCount) {
        state(playerCount > 2, "플레이어는 2명 이상이어야 합니다.");
        state(roundCount > 0, "라운드 수는 양수여야 합니다.");
    }
}

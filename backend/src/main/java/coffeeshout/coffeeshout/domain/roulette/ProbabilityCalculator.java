package coffeeshout.coffeeshout.domain.roulette;

import static org.springframework.util.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ProbabilityCalculator {
    private final Integer playerCount;
    private final Integer roundCount;
    private final Map<Integer, Double> adjustProbabilities;

    public ProbabilityCalculator(Integer playerCount, Integer roundCount) {
        validate(playerCount, roundCount);
        this.playerCount = playerCount;
        this.roundCount = roundCount;
        adjustProbabilities = initialAdjustProbability();
        adjustPositiveProbabilities();
        adjustNegativeProbabilities();
    }

    private void validate(Integer playerCount, Integer roundCount) {
        state(playerCount > 2, "플레이어는 2명 이상이어야 합니다.");
        state(roundCount > 0, "라운드 수는 양수여야 합니다.");
    }

    public double getAdjustProbability(int rank) {
        return adjustProbabilities.get(rank);
    }

    public double initialProbability() {
        return BigDecimal.valueOf(100).divide(
                BigDecimal.valueOf(playerCount),
                2,
                RoundingMode.HALF_DOWN
        ).doubleValue();
    }

    private void adjustPositiveProbabilities() {
        double current = maximumAdjustProbability();
        for (int i = playerCount; i >= playerCount - countEffectivePlayer(); --i) {
            adjustProbabilities.put(i, current);
            current -= computeAdjustProbabilityStep();
        }
    }

    private void adjustNegativeProbabilities() {
        double current = maximumAdjustProbability();
        for (int i = 1; i <= countEffectivePlayer(); ++i) {
            adjustProbabilities.put(i, -current);
            current += computeAdjustProbabilityStep();
        }
    }

    private double computeAdjustProbabilityStep() {
        return maximumAdjustProbability() / countEffectivePlayer();
    }

    private int maximumAdjustProbability() {
        return (int) (initialProbability() / roundCount);
    }

    private int countEffectivePlayer() {
        return playerCount / 2;
    }

    private Map<Integer, Double> initialAdjustProbability() {
        Map<Integer, Double> probability = new HashMap<>(playerCount);
        IntStream.range(1, playerCount).forEach(rank -> probability.put(rank, 0d));
        return probability;
    }
}

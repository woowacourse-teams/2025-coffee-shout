package coffeeshout.coffeeshout.domain.roulette;

import static org.springframework.util.Assert.state;

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
        // 생성자가 비대해서 정팩메 고려?
        validate(playerCount, roundCount);
        this.playerCount = playerCount;
        this.roundCount = roundCount;
        adjustProbabilities = initialAdjustProbability();
        adjustProbabilities();
    }

    private void validate(Integer playerCount, Integer roundCount) {
        state(playerCount > 2, "플레이어는 2명 이상이어야 합니다.");
        state(roundCount > 0, "라운드 수는 양수여야 합니다.");
    }

    private Map<Integer, Double> initialAdjustProbability() {
        Map<Integer, Double> probability = new HashMap<>(playerCount);
        IntStream.rangeClosed(1, playerCount).forEach(rank -> probability.put(rank, 0d));
        return probability;
    }

    public double getAdjustProbability(int rank) {
        state(adjustProbabilities.containsKey(rank), "해당 순위는 존재하지 않습니다. rank=" + rank);

        return adjustProbabilities.get(rank);
    }

    private double initialProbability() {
        return BigDecimal.valueOf(100).divide(
                BigDecimal.valueOf(playerCount),
                2,
                RoundingMode.HALF_DOWN
        ).doubleValue();
    }

    private void adjustProbabilities() {
        adjustTopPlayerProbabilities();
        adjustBottomPlayerProbabilities();
    }

    private void adjustTopPlayerProbabilities() {
        double current = getMaxAdjustableRangePerRound();
        for (int i = playerCount; i >= playerCount - countEffectivePlayer(); i--) {
            adjustProbabilities.put(i, current);
            current -= computeAdjustProbabilityStep();
        }
    }

    private void adjustBottomPlayerProbabilities() {
        double current = getMaxAdjustableRangePerRound();
        for (int i = 1; i <= countEffectivePlayer(); i++) {
            adjustProbabilities.put(i, -current);
            current += computeAdjustProbabilityStep();
        }
    }

    private double computeAdjustProbabilityStep() {
        return getMaxAdjustableRangePerRound() / countEffectivePlayer();
    }

    private int getMaxAdjustableRangePerRound() {
        return (int) (initialProbability() / roundCount);
    }

    private int countEffectivePlayer() {
        return playerCount / 2;
    }
}

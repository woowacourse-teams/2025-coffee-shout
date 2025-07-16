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
        // 생성자가 비대해서 정팩메 고려?
        validate(playerCount, roundCount);
        this.playerCount = playerCount;
        this.roundCount = roundCount;
        adjustProbabilities = initialAdjustProbability();
        adjustProbabilities();
    }

    private Map<Integer, Probability> initialAdjustProbabilities() {
        Map<Integer, Probability> probability = new HashMap<>(playerCount);
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

    private void adjustBottomPlayerProbabilities1() {
        Probability current = getMaxAdjustableRangePerRound();
        for (int i = playerCount; i >= playerCount - countEffectivePlayer(); i--) {
            adjustProbabilities.put(i, current);
            current -= computeAdjustProbabilityStep();
        }
    }

    private void adjustTopPlayerProbabilities() {
        Probability invertCurrent = getMaxAdjustableRangePerRound().invert();
        for (int i = 1; i <= countEffectivePlayer(); i++) {
            adjustProbabilities.put(i, -current);
            current += computeAdjustProbabilityStep();
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

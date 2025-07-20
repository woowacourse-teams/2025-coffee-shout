package coffeeshout.room.domain;

import static org.springframework.util.Assert.isTrue;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameResultType;
import coffeeshout.player.domain.Player;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class RouletteProbabilities {

    private final Map<Player, Probability> rouletteProbabilities;


    public RouletteProbabilities() {
        this.rouletteProbabilities = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    public void add(Player player) {
        rouletteProbabilities.put(player, Probability.ZERO_PERCENT);
        updateInitialProbabilities();
    }

    public void adjustProbabilities(MiniGameResult miniGameResult, int roundCount) {
        ProbabilityCalculator probabilityCalculator = new ProbabilityCalculator(getPlayerCount(), roundCount);
        for (Player player : rouletteProbabilities.keySet()) {
            final int rank = miniGameResult.getPlayerRank(player);
            final Probability probability = probabilityCalculator.calculateAdjustProbability(getPlayerCount(), rank);
            final MiniGameResultType resultType = MiniGameResultType.of(getPlayerCount(), rank);
            final Probability adjustedProbability = getProbability(player).adjust(resultType, probability);
            rouletteProbabilities.put(player, adjustedProbability);
        }
    }

    public void forEach(BiConsumer<Player, Probability> consumer) {
        rouletteProbabilities.forEach(consumer);
    }

    public Probability getProbability(Player player) {
        isTrue(rouletteProbabilities.containsKey(player), "존재하지 않는 사용자입니다.");
        return rouletteProbabilities.get(player);
    }

    private void updateInitialProbabilities() {
        final Probability initialProbability = ProbabilityCalculator.computeInitialProbability(getPlayerCount());
        rouletteProbabilities.keySet().forEach(player -> rouletteProbabilities.put(player, initialProbability));
    }

    private int getPlayerCount() {
        return rouletteProbabilities.size();
    }
}

package coffeeshout.coffeeshout.domain.player;

import static org.springframework.util.Assert.isTrue;

import coffeeshout.coffeeshout.domain.game.MiniGameResult;
import coffeeshout.coffeeshout.domain.roulette.Probability;
import coffeeshout.coffeeshout.domain.roulette.ProbabilityCalculator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class PlayersWithProbability {

    private static final int MAXIMUM_GUEST_COUNT = 9;

    private final Map<Player, Probability> adjustedProbabilities = new LinkedHashMap<>();

    public void join(Player joinPlayer) {
        isTrue(adjustedProbabilities.size() < MAXIMUM_GUEST_COUNT, "게임은 최대 9명까지 참여할 수 있습니다.");
        isTrue(!adjustedProbabilities.containsKey(joinPlayer), "이미 존재하는 플레이어 이름입니다.");
        adjustedProbabilities.put(joinPlayer, new Probability(0));
        updateInitialProbabilities();
    }

    public void adjustProbabilities(MiniGameResult miniGameResult, ProbabilityCalculator probabilityCalculator) {
        for (Player player : adjustedProbabilities.keySet()) {
            int rank = miniGameResult.getRank(player);
            Probability adjustProbability = probabilityCalculator.getAdjustProbability(rank);
            Probability totalProbability = getProbability(player).plus(adjustProbability);
            adjustedProbabilities.put(player, totalProbability);
        }
    }

    public int getPlayerCount() {
        return adjustedProbabilities.size();
    }

    public Probability getProbability(Player player) {
        isTrue(adjustedProbabilities.containsKey(player), "존재하지 않는 사용자입니다.");
        return adjustedProbabilities.get(player);
    }

    public void forEach(BiConsumer<Player, Probability> biConsumer) {
        adjustedProbabilities.forEach(biConsumer);
    }

    private void updateInitialProbabilities() {
        Probability initialProbability = ProbabilityCalculator.computeInitialProbability(getPlayerCount());
        adjustedProbabilities.keySet().forEach(player -> adjustedProbabilities.put(player, initialProbability));
    }
}

package coffeeshout.coffeeshout.domain.player;

import static org.springframework.util.Assert.isTrue;

import coffeeshout.coffeeshout.domain.roulette.Probability;
import coffeeshout.coffeeshout.domain.roulette.ProbabilityAdjuster;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class Players {

    private static final int MAXIMUM_GUEST_COUNT = 9;

    private final Map<Player, Probability> adjustedProbabilities = new HashMap<>();

    public void join(Player joinPlayer) {
        isTrue(adjustedProbabilities.size() < MAXIMUM_GUEST_COUNT, "게임은 최대 9명까지 참여할 수 있습니다.");
        isTrue(adjustedProbabilities.containsKey(joinPlayer), "이미 존재하는 플레이어 이름입니다.");
        adjustedProbabilities.put(joinPlayer, new Probability(0));
        updateInitialProbabilities();
    }

    public void updateInitialProbabilities() {
        Probability initialProbability = ProbabilityAdjuster.initialProbability(playerCount());
        adjustedProbabilities.keySet().forEach(player -> adjustedProbabilities.put(player, initialProbability));
    }

    public int playerCount() {
        return adjustedProbabilities.size();
    }
}

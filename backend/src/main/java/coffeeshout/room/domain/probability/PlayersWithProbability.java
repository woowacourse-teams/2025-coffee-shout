package coffeeshout.room.domain.probability;

import static org.springframework.util.Assert.isTrue;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class PlayersWithProbability {

    private final Map<Player, Probability> adjustedProbabilities;

    public PlayersWithProbability(Map<Player, Probability> playerProbabilityMap) {
        this.adjustedProbabilities = playerProbabilityMap;
    }

    public static PlayersWithProbability probability(
            List<Player> players,
            List<MiniGameResult> miniGameResults,
            ProbabilityCalculator calculator
    ) {
        return new PlayersWithProbability(players.stream().collect(Collectors.toMap(
                player -> player,
                player -> ProbabilityCalculator.computeInitialProbability(players.size()).adjust(computeAdjustProbability(player, miniGameResults, calculator))
        )));
    }

    private static AdjustmentProbability computeAdjustProbability(
            Player player,
            List<MiniGameResult> results,
            ProbabilityCalculator calculator
    ) {
        return results.stream()
                .map(result -> calculator.computeAdjustProbability(result.getPlayerRank(player)))
                .reduce(AdjustmentProbability.ZERO_PERCENT, AdjustmentProbability::plus);
    }

    public Probability getProbability(Player player) {
        isTrue(adjustedProbabilities.containsKey(player), "존재하지 않는 사용자입니다.");

        return adjustedProbabilities.get(player);
    }

    public void forEach(BiConsumer<Player, Probability> biConsumer) {
        adjustedProbabilities.forEach(biConsumer);
    }
}

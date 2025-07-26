package coffeeshout.room.domain.roulette;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameResultType;
import coffeeshout.room.domain.player.Player;
import java.util.LinkedHashMap;
import java.util.Map;

public class Roulette {

    private final RandomPicker randomPicker;
    private final Map<Player, Probability> playerProbabilities;

    public Roulette(RandomPicker randomGenerator) {
        this.randomPicker = randomGenerator;
        this.playerProbabilities = new LinkedHashMap<>();
    }

    public Player spin() {
        final RouletteRanges rouletteRanges = new RouletteRanges(playerProbabilities);
        final int randomNumber = randomPicker.nextInt(1, rouletteRanges.endValue());
        return rouletteRanges.pickPlayer(randomNumber);
    }

    public void join(Player player) {
        this.playerProbabilities.put(player, Probability.ZERO);
        final Probability probability = Probability.TOTAL.divide(getPlayerCount());
        for (Map.Entry<Player, Probability> entry : playerProbabilities.entrySet()) {
            entry.setValue(probability);
        }
    }

    public void adjustProbabilities(MiniGameResult miniGameResult, ProbabilityCalculator probabilityCalculator) {
        for (Player player : playerProbabilities.keySet()) {
            final int rank = miniGameResult.getPlayerRank(player);
            final Probability probability = probabilityCalculator.calculateAdjustProbability(getPlayerCount(), rank);
            final MiniGameResultType resultType = MiniGameResultType.of(getPlayerCount(), rank);
            final Probability adjustedProbability = getProbability(player).adjust(resultType, probability);
            playerProbabilities.put(player, adjustedProbability);
        }
    }

    public Probability getProbability(Player player) {
        return playerProbabilities.get(player);
    }

    private int getPlayerCount() {
        return playerProbabilities.size();
    }

    public Map<Player, Probability> getProbabilities() {
        return Map.copyOf(playerProbabilities);
    }
}

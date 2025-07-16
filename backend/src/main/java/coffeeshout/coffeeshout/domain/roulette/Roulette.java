package coffeeshout.coffeeshout.domain.roulette;

import coffeeshout.coffeeshout.domain.MiniGameResult;
import coffeeshout.coffeeshout.domain.player.Player;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

public class Roulette {

    private static final int MINIMUM_PLAYER_COUNT = 2;
    private static final int INITIAL_TOTAL_PROBABILITY = 10000;
    private static final int GAME_INFLUENCE_CONSTANT = 9600;
    private static final double PERCENTAGE_MULTIPLIER = 100.0;

    private final Map<Player, Integer> playerProbabilities = new LinkedHashMap<>();
    private final RandomGenerator randomGenerator;
    private final ProbabilityAdjuster probabilityCalculator;

    public Roulette(List<Player> players, int roundCount, RandomGenerator randomGenerator) {
        validate(players, roundCount);
        for (Player player : players) {
            this.playerProbabilities.put(player, INITIAL_TOTAL_PROBABILITY / players.size());
        }
        this.delta = getOptimalDelta(players, roundCount);
        this.randomGenerator = randomGenerator;
        this.probabilityCalculator = new ProbabilityAdjuster(players.size(), roundCount);
    }

    private void validate(List<Player> players, int round) {
        Assert.state(players.size() > MINIMUM_PLAYER_COUNT, "플레이어 인원 수는 2명 이상이여야 합니다.");
        Assert.state(round > 0, "라운드 수는 양수여야 합니다.");
    }

    public Player spin() {
        int randomNumber = randomGenerator.nextInt(getTotalProbability());

        for (Map.Entry<Player, Integer> entry : playerProbabilities.entrySet()) {
            Player player = entry.getKey();
            int probability = entry.getValue();

            randomNumber -= probability;

            if (randomNumber < 0) {
                return player;
            }
        }

        throw new IllegalStateException("잘못된 당첨 번호입니다. randomNumber = " + randomNumber);
    }

    public Map<Player, Double> getPlayerProbabilities() {
        Map<Player, Double> probabilities = new LinkedHashMap<>();
        List<Player> players = new ArrayList<>(playerProbabilities.keySet());

        for (Player player : players) {
            double rawPercentage = (double) getProbability(player) / getTotalProbability() * PERCENTAGE_MULTIPLIER;
            double roundedPercentage = Math.round(rawPercentage * PERCENTAGE_MULTIPLIER) / PERCENTAGE_MULTIPLIER;
            probabilities.put(player, roundedPercentage);
        }
        return probabilities;
    }

    private int getProbability(Player player) {
        Assert.state(playerProbabilities.containsKey(player), "존재하지 않는 Player입니다. Player=" + player);

        return playerProbabilities.get(player);
    }

    private int getPlayerCount() {
        return playerProbabilities.size();
    }

    private int getTotalProbability() {
        return playerProbabilities.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}

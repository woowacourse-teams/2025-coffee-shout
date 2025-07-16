package coffeeshout.coffeeshout.domain.roulette;

import coffeeshout.coffeeshout.domain.player.Player;
import coffeeshout.coffeeshout.domain.player.Players;

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

    public Player spin(Players players) {
        RouletteRanges rouletteRanges = new RouletteRanges(players);
        int randomNumber = randomGenerator.nextInt(1, rouletteRanges.endValue());
        return rouletteRanges.pickPlayer(randomNumber);
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

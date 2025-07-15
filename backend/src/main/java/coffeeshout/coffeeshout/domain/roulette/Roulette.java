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
    private final int delta;

    public Roulette(List<Player> players, int round, RandomGenerator randomGenerator) {
        validate(players, round);
        for (Player player : players) {
            this.playerProbabilities.put(player, INITIAL_TOTAL_PROBABILITY / players.size());
        }
        this.delta = getOptimalDelta(players, round);
        this.randomGenerator = randomGenerator;
    }

    private void validate(List<Player> players, int round) {
        Assert.state(players.size() > MINIMUM_PLAYER_COUNT, "플레이어 인원 수는 2명 이상이여야 합니다.");
        Assert.state(round > 0, "라운드 수는 양수여야 합니다.");
    }

    private int getOptimalDelta(List<Player> players, int round) {
        return GAME_INFLUENCE_CONSTANT / (players.size() * round);
    }

    public void adjustProbability(MiniGameResult miniGameResult) {
        double center = miniGameResult.getCenterRank();
        int maxGap = (int) Math.floor((getPlayerCount() - 1) / 2.0);

        for (Player player : playerProbabilities.keySet()) {
            int rank = miniGameResult.getRank(player);

            int sign = Double.compare(rank, center);
            int gap = getGap(rank, center);
            double diffProbability = delta / Math.pow(2, (double) maxGap - gap);
            int finalDiff = sign * (int) Math.round(diffProbability);

            changePlayerProbability(player, finalDiff);
        }
    }

    private void changePlayerProbability(Player player, int finalDiff) {
        int newProbability = getProbability(player) + finalDiff;
        Assert.state(newProbability >= 0 && newProbability <= INITIAL_TOTAL_PROBABILITY, "확률은 0보다 작거나, 100을 초과할 수 없습니다.");

        playerProbabilities.put(player, newProbability);
    }

    private int getGap(int rank, double center) {
        if (getPlayerCount() % 2 == 0) {
            return (int) (Math.abs(rank - center) - 0.5);
        }

        return (int) Math.abs(rank - center);
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

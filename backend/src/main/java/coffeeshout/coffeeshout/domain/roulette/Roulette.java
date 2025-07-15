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
    private static final int INITIAL_TOTAL_WEIGHT = 10000;
    private static final int GAME_INFLUENCE_CONSTANT = 9600;

    private final Map<Player, Integer> playerWeights = new LinkedHashMap<>();
    private final RandomGenerator randomGenerator;
    private final int delta;

    public Roulette(List<Player> players, int round, RandomGenerator randomGenerator) {
        validate(players, round);
        for (Player player : players) {
            this.playerWeights.put(player, INITIAL_TOTAL_WEIGHT / players.size());
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

    public void adjustWeight(MiniGameResult miniGameResult) {
        double center = miniGameResult.getCenterRank();
        int maxLevel = (int) Math.floor((getPlayerCount() - 1) / 2.0);

        for (Player player : playerWeights.keySet()) {
            int rank = miniGameResult.getRank(player);

            int sign = Double.compare(rank, center);
            int level = getLevel(rank, center);
            double diffWeight = delta / Math.pow(2, (double) maxLevel - level);
            int finalDiff = sign * (int) Math.round(diffWeight);

            changePlayerWeight(player, finalDiff);
        }
    }

    private void changePlayerWeight(Player player, int finalDiff) {
        int newWeight = getWeight(player) + finalDiff;
        Assert.state(newWeight >= 0 && newWeight <= INITIAL_TOTAL_WEIGHT, "확률은 0보다 작거나, 100을 초과할 수 없습니다.");

        playerWeights.put(player, newWeight);
    }

    private int getLevel(int rank, double center) {
        if (getPlayerCount() % 2 == 0) {
            return (int) (Math.abs(rank - center) - 0.5);
        }

        return (int) Math.abs(rank - center);
    }

    public Player spin() {
        int randomNumber = randomGenerator.nextInt(getTotalWeight());

        for (Map.Entry<Player, Integer> entry : playerWeights.entrySet()) {
            Player player = entry.getKey();
            int weight = entry.getValue();

            randomNumber -= weight;

            if (randomNumber < 0) {
                return player;
            }
        }

        throw new IllegalStateException("잘못된 당첨 번호입니다. randomNumber = " + randomNumber);
    }

    public Map<Player, Double> getPlayerProbabilities() {
        Map<Player, Double> probabilities = new LinkedHashMap<>();
        List<Player> players = new ArrayList<>(playerWeights.keySet());

        for (Player player : players) {
            double rawPercentage = (double) getWeight(player) / getTotalWeight() * 100.0;
            double roundedPercentage = Math.round(rawPercentage * 100.0) / 100.0;
            probabilities.put(player, roundedPercentage);
        }
        return probabilities;
    }

    private int getWeight(Player player) {
        Assert.state(playerWeights.containsKey(player), "존재하지 않는 Player입니다. Player=" + player);

        return playerWeights.get(player);
    }

    private int getPlayerCount() {
        return playerWeights.size();
    }

    private int getTotalWeight() {
        return playerWeights.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}

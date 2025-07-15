package coffeeshout.coffeeshout.domain;

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

    private final int delta;

    public Roulette(List<Player> players, int round) {
        validate(players, round);
        for (Player player : players) {
            this.playerWeights.put(player, INITIAL_TOTAL_WEIGHT / players.size());
        }
        this.delta = getOptimalDelta(players, round);
    }

    private void validate(List<Player> players, int round) {
        if (players.size() <= MINIMUM_PLAYER_COUNT) {
            throw new IllegalArgumentException("플레이어 인원 수는 2명 이상이여야 합니다.");
        }

        if (round <= 0) {
            throw new IllegalArgumentException("라운드 수는 양수여야 합니다.");
        }
    }

    private int getOptimalDelta(List<Player> players, int round) {
        return GAME_INFLUENCE_CONSTANT / (players.size() * round);
    }

    public void adjustWeight(MiniGameResult miniGameResult) {
        double center = miniGameResult.getCenterRank();
        int maxLevel = (int) Math.floor((getPlayerCount() - 1) / 2.0);

        for (int rank = 1; rank <= miniGameResult.getLastRank(); rank++) {
            Player player = miniGameResult.getPlayer(rank);
            int sign = Double.compare(rank, center);
            int level = getLevel(rank, center);
            double diffWeight = delta / Math.pow(2, (double) maxLevel - level);
            int finalDiff = sign * (int) Math.round(diffWeight);

            playerWeights.put(player, getWeight(player) + finalDiff);
        }
    }

    private int getLevel(int rank, double center) {
        if (getPlayerCount() % 2 == 0) {
            return (int) (Math.abs(rank - center) - 0.5);
        }

        return (int) Math.abs(rank - center);
    }

    private int getWeight(Player player) {
        Assert.state(playerWeights.containsKey(player), "존재하지 않는 Player입니다. Player=" + player);

        return playerWeights.get(player);
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

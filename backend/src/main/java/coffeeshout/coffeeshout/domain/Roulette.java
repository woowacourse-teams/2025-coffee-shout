package coffeeshout.coffeeshout.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

public class Roulette {

    private static final int MINIMUM_PLAYER_COUNT = 2;
    private static final int TOTAL_WEIGHT = 10000;
    private static final int GAME_INFLUENCE_CONSTANT = 9600;

    private final Map<Player, Integer> playerWeights = new LinkedHashMap<>();

    private final int delta;

    public Roulette(List<Player> players, int round) {
        for (Player player : players) {
            this.playerWeights.put(player, TOTAL_WEIGHT / players.size());
        }
        this.delta = getOptimalDelta(players, round);
    }

    private void validate(List<Player> players, ) {
        if (players.size() <= MINIMUM_PLAYER_COUNT){
            throw new IllegalArgumentException("플레이어 인원 수는 2명 이상이여야 합니다.");
        }
    }

    private int getOptimalDelta(List<Player> players, int round) {
        return GAME_INFLUENCE_CONSTANT / (players.size() * round);
    }

    public void adjustWeight(MiniGameResult miniGameResult) {
        for (int rank = 1; rank <= miniGameResult.getLastRank(); rank++) {
            Player player = miniGameResult.getPlayer(rank);
            int weight = getWeight(player);
            int diffWeight = (int) (rank - miniGameResult.getAverageRank()) * delta;
            playerWeights.put(player, weight + diffWeight);
        }
    }

    private int getWeight(Player player) {
        Assert.state(!playerWeights.containsKey(player), "존재하지 않는 Player입니다. Player=" + player);

        return playerWeights.get(player);
    }

    public Map<Player, Double> getPlayerProbabilities() {
        Map<Player, Double> probabilities = new LinkedHashMap<>();
        List<Player> players = new ArrayList<>(playerWeights.keySet());

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            double rawPercentage = (double) playerWeights.get(player) / TOTAL_WEIGHT * 100.0;
            double roundedPercentage = Math.round(rawPercentage * 100.0) / 100.0;
            probabilities.put(player, roundedPercentage);
        }
        return probabilities;
    }
}

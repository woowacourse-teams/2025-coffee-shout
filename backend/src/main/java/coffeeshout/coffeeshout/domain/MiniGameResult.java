package coffeeshout.coffeeshout.domain;

import coffeeshout.coffeeshout.domain.player.Player;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

public class MiniGameResult {

    private final Map<Player, Integer> ranks;

    public MiniGameResult(Map<Player, Integer> ranks) {
        this.ranks = ranks;
    }

    public int getLastRank() {
        return ranks.values()
                .stream()
                .max(Integer::compareTo)
                .orElseThrow(() -> new IllegalStateException("값이 존재해야 합니다."));
    }

    public double getCenterRank() {
        List<Integer> sortedRanks = ranks.values().stream().sorted().toList();
        int size = sortedRanks.size();
        int midIdx = size / 2;
        if (size % 2 == 1) {
            return sortedRanks.get(midIdx);
        }

        return (sortedRanks.get(midIdx - 1) + sortedRanks.get(midIdx)) / 2.0;
    }

    public int getRank(Player player) {
        Assert.state(ranks.containsKey(player), "존재하지 않는 player입니다. player=" + player);

        return ranks.get(player);
    }
}

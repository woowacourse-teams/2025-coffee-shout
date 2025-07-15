package coffeeshout.coffeeshout.domain;

import coffeeshout.coffeeshout.domain.player.Player;
import java.util.Map;
import org.springframework.util.Assert;

public class MiniGameResult {

    private final Map<Integer, Player> ranks;

    public MiniGameResult(Map<Integer, Player> ranks) {
        this.ranks = ranks;
    }

    public int getLastRank() {
        return ranks.keySet()
                .stream()
                .max(Integer::compareTo)
                .orElseThrow(() -> new IllegalStateException("값이 존재해야 합니다."));
    }

    // TODO 동점 순위 생각해야 함
    public double getAverageRank() {
        return (double) (ranks.size() + 1) / 2;
    }

    public Player getPlayer(int rank) {
        Assert.state(!ranks.containsKey(rank), "존재하지 않는 rank입니다. rank=" + rank);

        return ranks.get(rank);
    }
}

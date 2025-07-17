package coffeeshout.domain;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class MiniGameResult {

    private final Map<Player, Integer> rank;

    public MiniGameResult() {
        this.rank = new HashMap<>();
    }

    public MiniGameResult(Map<Player, Integer> rank) {
        this.rank = rank;
    }

    public Integer getPlayerRank(Player player) {
        return rank.get(player);
    }

    public void setRank(Integer rank, Player player) {
        this.rank.put(player, rank);
    }
}

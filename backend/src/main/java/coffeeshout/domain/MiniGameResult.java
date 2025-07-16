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

    public void setRank(Integer rank, Player player) {
        this.rank.put(player, rank);
    }
}

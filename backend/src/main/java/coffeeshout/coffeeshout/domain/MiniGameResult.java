package coffeeshout.coffeeshout.domain;

import static org.springframework.util.Assert.*;

import java.util.Map;

public class MiniGameResult {

    private final Map<Player, Integer> ranks;

    public MiniGameResult(Map<Player, Integer> ranks) {
        this.ranks = ranks;
    }

    public int getRank(Player player) {
        state(ranks.containsKey(player), "존재하지 않는 player입니다. player=" + player);
        return ranks.get(player);
    }
}

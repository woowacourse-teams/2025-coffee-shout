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

    public int getRank(Player player) {
        Assert.state(ranks.containsKey(player), "존재하지 않는 player입니다. player=" + player);

        return ranks.get(player);
    }
}

package coffeeshout.fixture;

import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.roulette.Probability;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PlayerProbabilities {

    public static final List<Player> PLAYERS = List.of(
            PlayerFixture.호스트꾹이(),
            PlayerFixture.게스트루키(),
            PlayerFixture.게스트엠제이(),
            PlayerFixture.게스트한스()
    );

    private PlayerProbabilities() {
    }

    public static Map<Player, Probability> 꾹이_루키_엠제이_한스() {
        Map<Player, Probability> playersWithProbability = new LinkedHashMap<>();
        PLAYERS.forEach(player -> playersWithProbability.put(player, new Probability(2500)));
        return playersWithProbability;
    }
}

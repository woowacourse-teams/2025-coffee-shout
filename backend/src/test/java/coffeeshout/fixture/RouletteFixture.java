package coffeeshout.fixture;

import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.roulette.Probability;
import coffeeshout.room.domain.roulette.Roulette;
import coffeeshout.room.domain.roulette.RoulettePicker;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.test.util.ReflectionTestUtils;

public final class RouletteFixture {

    private static final List<Player> playerList = List.of(
            PlayerFixture.호스트꾹이(),
            PlayerFixture.호스트루키(),
            PlayerFixture.호스트엠제이(),
            PlayerFixture.호스트한스()
    );

    private RouletteFixture() {
    }

    public static Roulette 고정_끝값_반환() {
        final Roulette roulette = new Roulette(new FixedLastValueGenerator());
        Map<Player, Probability> playerProbabilities = 꾹이_루키_엠제이_한스();
        ReflectionTestUtils.setField(roulette, "playerProbabilities", playerProbabilities);
        return roulette;
    }

    public static Roulette 랜덤_반환() {
        final Roulette roulette = new Roulette(new RoulettePicker());
        Map<Player, Probability> playerProbabilities = 꾹이_루키_엠제이_한스();
        ReflectionTestUtils.setField(roulette, "playerProbabilities", playerProbabilities);
        return roulette;
    }

    private static Map<Player, Probability> 꾹이_루키_엠제이_한스() {
        Map<Player, Probability> playersWithProbability = new LinkedHashMap<>();
        playerList.forEach(player -> playersWithProbability.put(player, new Probability(2500)));
        return playersWithProbability;
    }
}

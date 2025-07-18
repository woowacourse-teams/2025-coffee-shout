package coffeeshout.fixture;

import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.PlayersWithProbability;
import java.util.List;

public final class PlayersFixture {

    public static final List<Player> playerList = List.of(
            PlayerFixture.꾹이(),
            PlayerFixture.루키(),
            PlayerFixture.엠제이(),
            PlayerFixture.한스()
    );

    private PlayersFixture() {
    }

    public static PlayersWithProbability 꾹이_루키_엠제이_한스() {
        PlayersWithProbability playersWithProbability = new PlayersWithProbability();
        playerList.forEach(playersWithProbability::join);
        return playersWithProbability;
    }
}

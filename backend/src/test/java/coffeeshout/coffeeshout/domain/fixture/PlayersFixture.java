package coffeeshout.coffeeshout.domain.fixture;

import coffeeshout.coffeeshout.domain.player.Player;
import coffeeshout.coffeeshout.domain.player.Players;
import java.util.List;

public final class PlayersFixture {

    public static final List<Player> playerList = List.of(
            PlayerFixture.꾹이(),
            PlayerFixture.루키(),
            PlayerFixture.엠제이(),
            PlayerFixture.한스());

    private PlayersFixture() {
    }

    public static Players 꾹이_루키_엠제이_한스(){
        Players players = new Players();

        for (Player player : playerList){
            players.join(player);
        }

        return players;
    }
}

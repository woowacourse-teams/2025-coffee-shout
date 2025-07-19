package coffeeshout.fixture;

import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.PlayerInfos;
import java.util.List;

public final class PlayerInfosFixture {

    public static final List<Player> playerList = List.of(
            PlayerFixture.꾹이(),
            PlayerFixture.루키(),
            PlayerFixture.엠제이(),
            PlayerFixture.한스()
    );

    private PlayerInfosFixture() {
    }

    public static PlayerInfos 꾹이_루키_엠제이_한스() {
        PlayerInfos playerInfos = new PlayerInfos();
        playerList.forEach(playerInfos::join);
        return playerInfos;
    }
}

package coffeeshout.fixture;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.player.Player;
import java.util.Map;

public final class MiniGameResultFixture {

    private MiniGameResultFixture() {
    }

    public static MiniGameResult 한스_루키_꾹이_엠제이() {
        Player 한스 = PlayerFixture.한스();
        Player 루키 = PlayerFixture.루키();
        Player 꾹이 = PlayerFixture.꾹이();
        Player 엠제이 = PlayerFixture.엠제이();

        return new MiniGameResult(Map.of(한스, 1, 루키, 2, 꾹이, 3, 엠제이, 4));
    }
}



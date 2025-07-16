package coffeeshout.coffeeshout.domain.fixture;

import coffeeshout.coffeeshout.domain.game.MiniGameResult;
import coffeeshout.coffeeshout.domain.player.Player;
import java.util.Map;

public final class MiniGameResultFixture {

    private MiniGameResultFixture() {
    }

    public static MiniGameResult 한스_루키_꾹이_엠제이() {
        final Player 한스 = PlayerFixture.한스();
        final Player 루키 = PlayerFixture.루키();
        final Player 꾹이 = PlayerFixture.꾹이();
        final Player 엠제이 = PlayerFixture.엠제이();

        return new MiniGameResult(Map.of(한스, 1, 루키, 2, 꾹이, 3, 엠제이, 4));
    }
}



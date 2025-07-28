package coffeeshout.fixture;

import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;

public final class PlayerFixture {

    private PlayerFixture() {
    }

    public static Player 한스() {
        return new Player(new PlayerName("한스"), null);
    }

    public static Player 꾹이() {
        return new Player(new PlayerName("꾹이"), null);
    }

    public static Player 루키() {
        return new Player(new PlayerName("루키"), null);
    }

    public static Player 엠제이() {
        return new Player(new PlayerName("엠제이"), null);
    }
}

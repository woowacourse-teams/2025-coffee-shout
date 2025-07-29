package coffeeshout.fixture;

import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;

public final class PlayerFixture {

    private PlayerFixture() {
    }

    public static Player 호스트한스() {
        return Player.createHost(new PlayerName("한스"), null);
    }

    public static Player 호스트꾹이() {
        return Player.createHost(new PlayerName("꾹이"), null);
    }

    public static Player 호스트루키() {
        return Player.createHost(new PlayerName("루키"), null);
    }

    public static Player 호스트엠제이() {
        return Player.createHost(new PlayerName("엠제이"), null);
    }

    public static Player 게스트한스() {
        return Player.createGuest(new PlayerName("한스"), null);
    }

    public static Player 게스트꾹이() {
        return Player.createGuest(new PlayerName("꾹이"), null);
    }

    public static Player 게스트루키() {
        return Player.createGuest(new PlayerName("루키"), null);
    }

    public static Player 게스트엠제이() {
        return Player.createGuest(new PlayerName("엠제이"), null);
    }
}

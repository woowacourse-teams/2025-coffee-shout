package coffeeshout.coffeeshout.domain.fixture;

import coffeeshout.coffeeshout.domain.player.Player;

public final class PlayerFixture {

    private PlayerFixture() {
    }

    public static Player 한스() {
        return new Player(1L, "한스", null);
    }

    public static Player 꾹이() {
        return new Player(2L, "꾹이", null);
    }

    public static Player 루키() {
        return new Player(3L, "꾹이", null);
    }

    public static Player 엠제이() {
        return new Player(4L, "엠제이", null);
    }
}

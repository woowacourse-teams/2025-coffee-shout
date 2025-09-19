package coffeeshout.fixture;

import coffeeshout.room.domain.player.Players;

public final class PlayersFixture {

    public static Players 호스트꾹이_루키_엠제이_한스() {
        final Players players = new Players();
        players.join(PlayerFixture.호스트꾹이());
        players.join(PlayerFixture.게스트루키());
        players.join(PlayerFixture.게스트엠제이());
        players.join(PlayerFixture.게스트한스());
        return players;
    }

    public static Players 루키_엠제이_한스_리스트() {
        final Players players = new Players();
        players.join(PlayerFixture.게스트루키());
        players.join(PlayerFixture.게스트엠제이());
        players.join(PlayerFixture.게스트한스());
        return players;
    }

    private PlayersFixture() {
    }
}

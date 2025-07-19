package coffeeshout.fixture;

import coffeeshout.room.domain.FixedLastValueGenerator;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.PlayerInfos;
import coffeeshout.room.domain.Room;

public final class RoomFixture {

    private RoomFixture() {
    }

    public static Room 호스트_꾹이() {
        final PlayerInfos playerInfos = new PlayerInfos(PlayerFixture.꾹이());

        return new Room(new JoinCode("A4B2C"), playerInfos, new FixedLastValueGenerator());
    }

    public static Room 꾹이_루키_엠제이_한스() {
        return new Room(new JoinCode("A4B2C"), PlayerInfosFixture.꾹이_루키_엠제이_한스(), new FixedLastValueGenerator());
    }
}

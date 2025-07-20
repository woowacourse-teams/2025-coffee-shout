package coffeeshout.fixture;

import coffeeshout.room.domain.FixedLastValueGenerator;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.RouletteRoom;
import org.springframework.test.util.ReflectionTestUtils;

public final class RoomFixture {

    private RoomFixture() {
    }

    public static RouletteRoom 호스트_꾹이() {
        RouletteRoom room = new RouletteRoom(
                new JoinCode("A4B2C"),
                PlayerFixture.꾹이(),
                new FixedLastValueGenerator()
        );
        return room;
    }
}

package coffeeshout.fixture;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import org.springframework.test.util.ReflectionTestUtils;

public final class RoomFixture {

    private RoomFixture() {
    }

    public static Room 호스트_꾹이() {
        Room room = new Room(new JoinCode("A4B2C"), PlayerFixture.꾹이());
        ReflectionTestUtils.setField(room, "roulette", RouletteFixture.고정_끝값_반환());

        return room;
    }
}

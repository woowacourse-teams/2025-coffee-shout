package coffeeshout.fixture;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.PlayerName;
import org.springframework.test.util.ReflectionTestUtils;

public final class RoomFixture {

    private RoomFixture() {
    }

    public static Room 호스트_꾹이() {
        Room room = Room.createNewRoom(new JoinCode("A4B2C"), PlayerFixture.꾹이().getName(), MenuFixture.아메리카노());
        ReflectionTestUtils.setField(room, "roulette", RouletteFixture.고정_끝값_반환());
        room.joinGuest(PlayerName.from("엠제이"), MenuFixture.아메리카노());
        room.joinGuest(PlayerName.from("루키"), MenuFixture.아메리카노());
        return room;
    }
}

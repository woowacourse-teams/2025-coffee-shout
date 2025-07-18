package coffeeshout.fixture;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;

public final class RoomFixture {

    private RoomFixture() {
    }

    public static Room 호스트_꾹이() {
        return Room.builder()
                .host(PlayerFixture.꾹이())
                .joinCode(new JoinCode("A4B2C"))
                .roulette(RouletteFixture.고정_끝값_반환())
                .build();
    }
}

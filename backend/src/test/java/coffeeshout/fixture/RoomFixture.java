package coffeeshout.fixture;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Players;
import org.springframework.test.util.ReflectionTestUtils;

public final class RoomFixture {

    private RoomFixture() {
    }

    public static Room 호스트_꾹이() {
        Room room = new Room(new JoinCode("A4B2C"), PlayerFixture.꾹이().getName(), MenuFixture.아메리카노());
        ReflectionTestUtils.setField(room, "roulette", RouletteFixture.고정_끝값_반환());
        Players players = new Players();
        ReflectionTestUtils.setField(players, "players", PlayerProbabilities.PLAYERS);
        ReflectionTestUtils.setField(room, "players", players);
        return room;
    }
}

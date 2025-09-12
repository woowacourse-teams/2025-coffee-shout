package coffeeshout.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.repository.RoomRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RoomTest {


    @Autowired
    private RoomRepository roomRepository;

    @Test
    void RoomTest() {
        // given
        final JoinCode joinCode = new JoinCode("ABCDE");
        final List<Player> players = List.of(
                new Player(new PlayerName("hans1")),
                new Player(new PlayerName("hans2"))
        );
        final RoomState roomState = RoomState.PLAYING;
        final Map<Player, RoomState> map = Map.of(new Player(new PlayerName("asb")), RoomState.DONE);
        final SomeInterface some = new SomeImplementation();
        final Room room = new Room(joinCode, players, roomState, map, some);

        // when
        final Room savedRoom = roomRepository.save(room);

        // then
        assertThat(savedRoom.getJoinCode().getValue()).isEqualTo("ABCDE");

        final Optional<Room> foundRoom = roomRepository.findById(joinCode);

        assertThat(foundRoom).isPresent();

        final Room gotRoom = foundRoom.get();

        assertThat(gotRoom.getRoomState()).isEqualTo(roomState);
        assertThat(gotRoom.getJoinCode().getValue()).isEqualTo("ABCDE");
        assertThat(gotRoom.getSomeInterface().getA()).isEqualTo(1);
        assertThat(gotRoom.getHost().get(0).getName().value()).isEqualTo("hans1");

        System.out.println(room.getMap());
    }
}

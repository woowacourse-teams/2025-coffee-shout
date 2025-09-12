package coffeeshout.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.repository.RoomRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RoomTest {

    @Autowired
    private RoomRepository roomRepository;

    class SomeClass implements SomeInterface {
        private final int a = 0;
    }

    @Test
    void RoomTest() {
        // given
        final Room room = new Room(
                "ABCDE",
                List.of(
                        new Player(new PlayerName("hans1")),
                        new Player(new PlayerName("hans2"))
                ),
                RoomState.PLAYING
        );

        // when
        final Room savedRoom = roomRepository.save(room);

        // then
        assertThat(savedRoom.getJoinCode()).isEqualTo("ABCDE");

        final Optional<Room> foundRoom = roomRepository.findById("ABCDE");

        assertThat(foundRoom).isPresent();

        final Room gotRoom = foundRoom.get();

        assertThat(gotRoom.getRoomState()).isEqualTo(RoomState.PLAYING);
        assertThat(gotRoom.getJoinCode()).isEqualTo("ABCDE");
        assertThat(gotRoom.getHost().get(0).getName().value()).isEqualTo("hans1");


    }

}

package coffeeshout.fixture;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.MenuRepository;
import coffeeshout.room.domain.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

@Component
public class TestDataHelper {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MenuRepository menuRepository;

    public Room createDummyRoom(String joinCode, String hostName) {
        Menu menu = menuRepository.findById(1L).orElseThrow();
        Room room = new Room(new JoinCode(joinCode), new PlayerName(hostName), menu);
        return roomRepository.save(room);
    }

    public Room createDummyPlayingRoom(String joinCode, String hostName) {
        Menu menu = menuRepository.findById(1L).orElseThrow();
        Room room = new Room(new JoinCode(joinCode), new PlayerName(hostName), menu);
        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);
        return roomRepository.save(room);
    }
}


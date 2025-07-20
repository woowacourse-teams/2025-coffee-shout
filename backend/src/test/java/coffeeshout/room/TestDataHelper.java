package coffeeshout.room;

import coffeeshout.player.domain.Menu;
import coffeeshout.player.domain.Player;
import coffeeshout.player.domain.repository.MenuRepository;
import coffeeshout.room.domain.JavaRandomGenerator;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.RouletteRoom;
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

    public RouletteRoom createDummyRoom(String joinCode, String hostName) {
        Menu menu = menuRepository.findById(1L).orElseThrow();
        Player host = new Player(hostName, menu);
        RouletteRoom room = new RouletteRoom(new JoinCode(joinCode), host, new JavaRandomGenerator());
        return roomRepository.save(room);
    }

    public RouletteRoom createDummyPlayingRoom(String joinCode, String hostName) {
        Menu menu = menuRepository.findById(1L).orElseThrow();
        Player host = new Player(hostName, menu);
        RouletteRoom room = new RouletteRoom(new JoinCode(joinCode), host, new JavaRandomGenerator());
        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);
        return roomRepository.save(room);
    }
}


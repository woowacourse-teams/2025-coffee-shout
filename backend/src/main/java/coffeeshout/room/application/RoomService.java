package coffeeshout.room.application;

import coffeeshout.player.domain.Menu;
import coffeeshout.player.domain.MenuFinder;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.JoinCodeGenerator;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomFinder;
import coffeeshout.room.domain.RoomSaver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomFinder roomFinder;
    private final RoomSaver roomSaver;
    private final MenuFinder menuFinder;
    private final JoinCodeGenerator joinCodeGenerator;

    public Room createRoom(String hostName, Long menuId) {
        final Menu menu = menuFinder.findById(menuId);
        final JoinCode joinCode = joinCodeGenerator.generate();
        final Room room = Room.createNewRoom(joinCode, hostName, menu);

        return roomSaver.save(room);
    }

    public Room enterRoom(String joinCode, String guestName, Long menuId) {
        final Menu menu = menuFinder.findById(menuId);
        final Room room = roomFinder.findByJoinCode(JoinCode.from(joinCode));

        room.joinGuest(guestName, menu);

        return roomSaver.save(room);
    }

    public List<Player> getAllPlayers(Long roomId) {
        final Room room = roomFinder.findById(roomId);

        return room.getPlayers();
    }

    public List<Player> selectMenu(Long roomId, String playerName, Long menuId) {
        final Room room = roomFinder.findById(roomId);
        final Menu menu = menuFinder.findById(menuId);

        final Player player = room.findPlayer(playerName);
        player.selectMenu(menu);

        return room.getPlayers();
    }
}

package coffeeshout.room.application;

import coffeeshout.player.domain.Menu;
import coffeeshout.player.domain.MenuFinder;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.JavaRandomGenerator;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.service.JoinCodeGenerator;
import coffeeshout.room.domain.service.RoomFinder;
import coffeeshout.room.domain.service.RoomSaver;
import coffeeshout.room.domain.RouletteRoom;
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

    public RouletteRoom createRoom(String hostName, Long menuId) {
        final Menu menu = menuFinder.findById(menuId);
        final Player host = new Player(hostName, menu);

        final JoinCode joinCode = joinCodeGenerator.generate();
        final RouletteRoom room = new RouletteRoom(joinCode, host, new JavaRandomGenerator());

        return roomSaver.save(room);
    }

    public RouletteRoom enterRoom(String joinCode, String guestName, Long menuId) {
        final Menu menu = menuFinder.findById(menuId);
        final Player guest = new Player(guestName, menu);

        final RouletteRoom room = roomFinder.findByJoinCode(new JoinCode(joinCode));
        room.joinGuest(guest);

        return roomSaver.save(room);
    }

    public List<Player> getAllPlayers(Long roomId) {
        final RouletteRoom room = roomFinder.findById(roomId);

        return room.getPlayers();
    }

    public List<Player> selectMenu(Long roomId, String playerName, Long menuId) {
        final RouletteRoom room = roomFinder.findById(roomId);
        final Menu menu = menuFinder.findById(menuId);

        final Player player = room.findPlayer(playerName);
        player.selectMenu(menu);

        return room.getPlayers();
    }
}

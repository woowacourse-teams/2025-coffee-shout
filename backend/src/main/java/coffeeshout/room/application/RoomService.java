package coffeeshout.room.application;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.MenuFinder;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.roulette.Probability;
import coffeeshout.room.domain.service.JoinCodeGenerator;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomQueryService roomQueryService;
    private final RoomCommandService roomCommandService;
    private final MenuFinder menuFinder;
    private final JoinCodeGenerator joinCodeGenerator;

    public Room createRoom(String hostName, Long menuId) {
        final Menu menu = menuFinder.findById(menuId);
        final JoinCode joinCode = joinCodeGenerator.generate();
        final Room room = Room.createNewRoom(joinCode, PlayerName.from(hostName), menu);

        return roomCommandService.save(room);
    }

    public Room enterRoom(String joinCode, String guestName, Long menuId) {
        final Menu menu = menuFinder.findById(menuId);
        final Room room = roomQueryService.findByJoinCode(JoinCode.from(joinCode));

        room.joinGuest(PlayerName.from(guestName), menu);

        return roomCommandService.save(room);
    }

    public List<Player> getAllPlayers(Long roomId) {
        final Room room = roomQueryService.findById(roomId);

        return room.getPlayers();
    }

    public List<Player> selectMenu(Long roomId, String playerName, Long menuId) {
        final Room room = roomQueryService.findById(roomId);
        final Menu menu = menuFinder.findById(menuId);

        final Player player = room.findPlayer(PlayerName.from(playerName));
        player.selectMenu(menu);

        return room.getPlayers();
    }

    public Map<Player, Probability> getProbabilities(Long roomId) {
        final Room room = roomQueryService.findById(roomId);

        return room.getProbabilities();
    }
}

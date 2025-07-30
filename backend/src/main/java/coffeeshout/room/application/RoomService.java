package coffeeshout.room.application;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.roulette.Probability;
import coffeeshout.room.domain.service.JoinCodeGenerator;
import coffeeshout.room.domain.service.MenuQueryService;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomQueryService roomQueryService;
    private final RoomCommandService roomCommandService;
    private final MenuQueryService menuQueryService;
    private final JoinCodeGenerator joinCodeGenerator;

    public Room createRoom(String hostName, Long menuId) {
        final Menu menu = menuQueryService.findById(menuId);
        final JoinCode joinCode = joinCodeGenerator.generate();
        final Room room = Room.createNewRoom(joinCode, new PlayerName(hostName), menu);

        return roomCommandService.save(room);
    }

    public Room enterRoom(String joinCode, String guestName, Long menuId) {
        final Menu menu = menuQueryService.findById(menuId);
        final Room room = roomQueryService.findByJoinCode(new JoinCode(joinCode));

        room.joinGuest(new PlayerName(guestName), menu);

        return roomCommandService.save(room);
    }

    public List<Player> getAllPlayers(String joinCode) {
        final Room room = roomQueryService.findByJoinCode(new JoinCode(joinCode));

        return room.getPlayers();
    }

    public List<Player> selectMenu(String joinCode, String playerName, Long menuId) {
        final Room room = roomQueryService.findByJoinCode(new JoinCode(joinCode));
        final Menu menu = menuQueryService.findById(menuId);

        final Player player = room.findPlayer(new PlayerName(playerName));
        player.selectMenu(menu);

        return room.getPlayers();
    }

    public Map<Player, Probability> getProbabilities(String joinCode) {
        final Room room = roomQueryService.findByJoinCode(new JoinCode(joinCode));

        return room.getProbabilities();
    }

    public List<MiniGameType> getAllMiniGames() {
        return Arrays.stream(MiniGameType.values())
                .toList();
    }

    public List<MiniGameType> updateMiniGames(String joinCode, String hostName, List<MiniGameType> miniGameTypes) {
        final Room room = roomQueryService.findByJoinCode(new JoinCode(joinCode));
        room.clearMiniGames();

        miniGameTypes.forEach(miniGameType -> {
            final Playable miniGame = miniGameType.createMiniGame();
            room.addMiniGame(new PlayerName(hostName), miniGame);
        });

        return room.getAllMiniGame().stream()
                .map(Playable::getMiniGameType)
                .toList();
    }

    public boolean roomExists(String joinCode) {
        return roomQueryService.existsByJoinCode(new JoinCode(joinCode));
    }

    public Player spinRoulette(String joinCode, String hostName) {
        final Room room = roomQueryService.findByJoinCode(new JoinCode(joinCode));
        final Player host = room.findPlayer(new PlayerName(hostName));

        roomCommandService.delayCleanUp(room, Duration.ofHours(1));

        return room.spinRoulette(host);
    }

    public boolean isGuestNameDuplicated(String joinCode, String guestName) {
        final Room room = roomQueryService.findByJoinCode(new JoinCode(joinCode));

        return room.hasDuplicatePlayerName(new PlayerName(guestName));
    }
}

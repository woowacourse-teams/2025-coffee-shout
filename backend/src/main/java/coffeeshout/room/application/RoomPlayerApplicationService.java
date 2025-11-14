package coffeeshout.room.application;

import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.service.PlayerCommandService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomPlayerApplicationService {

    private final PlayerCommandService playerCommandService;

    public List<Player> changePlayerReadyState(String joinCode, String playerName, Boolean isReady) {
        return playerCommandService.changePlayerReadyState(joinCode, playerName, isReady);
    }

    public List<Player> getAllPlayers(String joinCode) {
        return playerCommandService.getAllPlayers(joinCode);
    }

    public List<Player> selectMenu(String joinCode, String playerName, Long menuId) {
        return playerCommandService.selectMenu(joinCode, playerName, menuId);
    }

    public boolean isGuestNameDuplicated(String joinCode, String guestName) {
        return playerCommandService.isGuestNameDuplicated(joinCode, guestName);
    }

    public boolean removePlayer(String joinCode, String playerName) {
        return playerCommandService.removePlayer(joinCode, playerName);
    }
}

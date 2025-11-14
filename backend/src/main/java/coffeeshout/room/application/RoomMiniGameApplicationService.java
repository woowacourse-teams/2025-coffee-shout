package coffeeshout.room.application;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.service.MiniGameCommandService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomMiniGameApplicationService {

    private final MiniGameCommandService miniGameCommandService;

    public List<MiniGameType> updateMiniGames(String joinCode, String hostName, List<MiniGameType> miniGameTypes) {
        return miniGameCommandService.updateMiniGames(joinCode, hostName, miniGameTypes);
    }

    public List<MiniGameType> getAllMiniGames() {
        return miniGameCommandService.getAllMiniGames();
    }

    public Map<Player, MiniGameScore> getMiniGameScores(String joinCode, MiniGameType miniGameType) {
        return miniGameCommandService.getMiniGameScores(joinCode, miniGameType);
    }

    public MiniGameResult getMiniGameRanks(String joinCode, MiniGameType miniGameType) {
        return miniGameCommandService.getMiniGameRanks(joinCode, miniGameType);
    }

    public List<MiniGameType> getSelectedMiniGames(String joinCode) {
        return miniGameCommandService.getSelectedMiniGames(joinCode);
    }

    public List<Playable> getRemainingMiniGames(String joinCode) {
        return miniGameCommandService.getRemainingMiniGames(joinCode);
    }
}

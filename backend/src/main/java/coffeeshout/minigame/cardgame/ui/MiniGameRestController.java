package coffeeshout.minigame.cardgame.ui;

import coffeeshout.minigame.MiniGameResult;
import coffeeshout.minigame.MiniGameScore;
import coffeeshout.minigame.MiniGameType;
import coffeeshout.minigame.cardgame.ui.response.MiniGameRanksResponse;
import coffeeshout.minigame.cardgame.ui.response.MiniGameScoresResponse;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.player.Player;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/minigames")
@RequiredArgsConstructor
public class MiniGameRestController {

    private final RoomService roomService;

    @GetMapping("/scores")
    public ResponseEntity<MiniGameScoresResponse> getScores(
            @RequestParam String joinCode,
            @RequestParam MiniGameType miniGameType
    ) {
        Map<Player, MiniGameScore> result = roomService.getMiniGameScores(joinCode, miniGameType);

        return ResponseEntity.ok(MiniGameScoresResponse.from(result));
    }

    @GetMapping("/ranks")
    public ResponseEntity<MiniGameRanksResponse> getRanks(
            @RequestParam String joinCode,
            @RequestParam MiniGameType miniGameType
    ) {
        final MiniGameResult result = roomService.getMiniGameRanks(joinCode, miniGameType);

        return ResponseEntity.ok(MiniGameRanksResponse.from(result));
    }
}

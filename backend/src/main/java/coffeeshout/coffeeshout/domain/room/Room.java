package coffeeshout.coffeeshout.domain.room;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.state;

import coffeeshout.coffeeshout.domain.game.MiniGame;
import coffeeshout.coffeeshout.domain.game.MiniGameResult;
import coffeeshout.coffeeshout.domain.player.Player;
import coffeeshout.coffeeshout.domain.player.PlayersWithProbability;
import coffeeshout.coffeeshout.domain.roulette.ProbabilityAdjuster;
import coffeeshout.coffeeshout.domain.roulette.Roulette;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Room {

    private Long id;

    private JoinCode joinCode;

    private Player host;

    private PlayersWithProbability playersWithProbability = new PlayersWithProbability();

    private Roulette roulette;

    private List<MiniGame> miniGames = new ArrayList<>();

    private RoomState roomState;

    public static final int MAXIMUM_GUEST_COUNT = 9;
    public static final int MINIMUM_GUEST_COUNT = 2;

    @Builder
    public Room(JoinCode joinCode, Roulette roulette, Player host) {
        this.joinCode = joinCode;
        this.roulette = roulette;
        this.host = host;
        playersWithProbability.join(host);
        this.roomState = RoomState.READY;
    }

    public void joinPlayer(Player joinPlayer) {
        isTrue(roomState == RoomState.READY, "READY 상태에서만 참여 가능합니다.");
        playersWithProbability.join(joinPlayer);
    }

    public void setMiniGame(List<MiniGame> miniGames) {
        state(miniGames.size() <= 5, "미니게임은 5개 이하여야 합니다.");
        this.miniGames = miniGames;
    }

    // TODO 미니게임 결과 반영
    public void applyMiniGameResult(MiniGameResult miniGameResult){
        playersWithProbability.adjustProbabilities(miniGameResult, new ProbabilityAdjuster(playersWithProbability.getPlayerCount(), miniGames.size()));
    }

    // TODO: 플레이어가 방에서 나가는 로직 필요

    public boolean hasNoMiniGames() {
        return miniGames.isEmpty();
    }

    public boolean isInPlayingState() {
        return roomState == RoomState.PLAYING;
    }

    public boolean hasEnoughPlayers() {
        return playersWithProbability.getPlayerCount() >= MINIMUM_GUEST_COUNT && playersWithProbability.getPlayerCount() <= MAXIMUM_GUEST_COUNT;
    }

    public Player startRoulette() {
        state(hasEnoughPlayers(), "룰렛은 2~9명의 플레이어가 참여해야 시작할 수 있습니다.");
        state(isInPlayingState(), "게임 중일때만 룰렛을 돌릴 수 있습니다.");
        Player losePlayer = roulette.spin(playersWithProbability);
        roomState = RoomState.DONE;
        return losePlayer;
    }

    public boolean isHost(Player player) {
        return host.equals(player);
    }

//    TODO: 미니게임 플레이 어떻게 할까
//    public void playMiniGame(int miniGameIndex) {
//       MiniGameResult result = miniGames.get(miniGameIndex).play();
//       this.roomState = RoomState.PLAYING;
//       applyMiniGameResult(result);
//    }

    // 이벤트 수신하는 메서드
}

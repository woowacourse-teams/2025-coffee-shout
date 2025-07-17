package coffeeshout.domain;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.state;

import jakarta.persistence.Entity;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Room {

    private static final int MAXIMUM_GUEST_COUNT = 9;
    private static final int MINIMUM_GUEST_COUNT = 2;

    private Long id;

    private JoinCode joinCode;

    private Player host;

    private PlayersWithProbability playersWithProbability = new PlayersWithProbability();

    private Roulette roulette;

    private List<MiniGame> miniGames = new ArrayList<>();

    private RoomState roomState;

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
        playersWithProbability.adjustProbabilities(miniGameResult, new ProbabilityCalculator(playersWithProbability.getPlayerCount(), miniGames.size()));
    }

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
        final Player losePlayer = roulette.spin(playersWithProbability);
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

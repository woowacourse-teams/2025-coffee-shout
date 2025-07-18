package coffeeshout.room.domain;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.state;

import coffeeshout.minigame.domain.MiniGame;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;
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

    private PlayersWithProbability playersWithProbability;

    private Roulette roulette;

    private List<MiniGame> miniGames;

    private RoomState roomState;

    @Builder
    public Room(JoinCode joinCode, Roulette roulette, Player host) {
        this.joinCode = joinCode;
        this.roulette = roulette;
        this.host = host;
        this.playersWithProbability = new PlayersWithProbability();
        this.roomState = RoomState.READY;
        this.miniGames = new ArrayList<>();

        playersWithProbability.join(host);
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
    public void applyMiniGameResult(MiniGameResult miniGameResult) {
        playersWithProbability.adjustProbabilities(miniGameResult,
                new ProbabilityCalculator(playersWithProbability.getPlayerCount(), miniGames.size()));
    }

    public boolean hasNoMiniGames() {
        return miniGames.isEmpty();
    }

    public boolean isInPlayingState() {
        return roomState == RoomState.PLAYING;
    }

    public boolean hasEnoughPlayers() {
        return playersWithProbability.getPlayerCount() >= MINIMUM_GUEST_COUNT
                && playersWithProbability.getPlayerCount() <= MAXIMUM_GUEST_COUNT;
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

    public List<Player> getPlayers() {
        return playersWithProbability.getPlayers();
    }

    public Player findPlayer(String playerName) {
        return playersWithProbability.getPlayers().stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("사용지가 존재하지 않습니다."));
    }

//    TODO: 미니게임 플레이 어떻게 할까
//    public void playMiniGame(int miniGameIndex) {
//       MiniGameResult result = miniGames.get(miniGameIndex).play();
//       this.roomState = RoomState.PLAYING;
//       applyMiniGameResult(result);
//    }

    // 이벤트 수신하는 메서드
}

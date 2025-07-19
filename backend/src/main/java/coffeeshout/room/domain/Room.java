package coffeeshout.room.domain;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.state;

import coffeeshout.minigame.domain.MiniGame;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class Room {

    private final JoinCode joinCode;
    private final Player host;
    private final Players players;
    private final Roulette roulette;
    private List<MiniGame> miniGames;
    private Long id;
    private RoomState roomState;

    public Room(JoinCode joinCode, PlayerInfos playerInfos) {
        this.joinCode = joinCode;
        this.roulette = new Roulette(playerInfos, new JavaRandomGenerator());
        this.host = playerInfos.getHost();
        this.players = new Players(playerInfos);
        this.roomState = RoomState.READY;
        this.miniGames = new ArrayList<>();
    }

    public Room(JoinCode joinCode, PlayerInfos playerInfos, RandomGenerator randomGenerator) {
        this.joinCode = joinCode;
        this.roulette = new Roulette(playerInfos, randomGenerator);
        this.host = playerInfos.getHost();
        this.players = new Players(playerInfos);
        this.roomState = RoomState.READY;
        this.miniGames = new ArrayList<>();
    }

    public void joinPlayer(Player joinPlayer) {
        isTrue(roomState == RoomState.READY, "READY 상태에서만 참여 가능합니다.");
        players.join(joinPlayer);
    }

    public void setMiniGame(List<MiniGame> miniGames) {
        state(miniGames.size() <= 5, "미니게임은 5개 이하여야 합니다.");
        this.miniGames = miniGames;
    }

    // TODO 미니게임 결과 반영
    public void applyMiniGameResult(MiniGameResult miniGameResult) {
        roulette.adjustProbabilities(miniGameResult, miniGames.size());
    }

    public boolean hasNoMiniGames() {
        return miniGames.isEmpty();
    }

    public boolean isInPlayingState() {
        return roomState == RoomState.PLAYING;
    }

    public boolean hasEnoughPlayers() {
        return players.hasEnoughPlayers();
    }

    public Player startRoulette() {
        state(hasEnoughPlayers(), "룰렛은 2~9명의 플레이어가 참여해야 시작할 수 있습니다.");
        state(isInPlayingState(), "게임 중일때만 룰렛을 돌릴 수 있습니다.");

        final Player winner = roulette.spin();
        roomState = RoomState.DONE;
        return winner;
    }

    public boolean isHost(Player player) {
        return host.equals(player);
    }

    public List<Player> getPlayers() {
        return players.getPlayers();
    }

    public Player findPlayer(String playerName) {
        return players.getPlayer(playerName);
    }

//    TODO: 미니게임 플레이 어떻게 할까
//    public void playMiniGame(int miniGameIndex) {
//       MiniGameResult result = miniGames.get(miniGameIndex).play();
//       this.roomState = RoomState.PLAYING;
//       applyMiniGameResult(result);
//    }

    // 이벤트 수신하는 메서드
}

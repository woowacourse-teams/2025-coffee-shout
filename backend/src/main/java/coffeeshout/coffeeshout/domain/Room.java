package coffeeshout.coffeeshout.domain;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.state;

import coffeeshout.coffeeshout.domain.player.Player;
import coffeeshout.coffeeshout.domain.player.Players;
import coffeeshout.coffeeshout.domain.roulette.Roulette;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class Room {

    private Long id;

    private JoinCode joinCode;

    private Player host;

    private Players players = new Players();

//    private Roulette roulette;

    private List<MiniGame> miniGames = new ArrayList<>();

    private RoomState roomState;

    public static final int MAXIMUM_GUEST_COUNT = 9;
    public static final int MINIMUM_GUEST_COUNT = 2;

    public Room(JoinCode joinCode, Roulette roulette) {
        this.joinCode = joinCode;
        this.roulette = roulette;
        this.roomState = RoomState.READY;
    }

    public Room(JoinCode joinCode, Roulette roulette, Player host) {
        this.joinCode = joinCode;
        this.roulette = roulette;
        this.host = host;
        players.join(host);
        this.roomState = RoomState.READY;
    }

    public void changeHost(Player host) {
        this.host = host;
    }

    public void joinPlayer(Player joinPlayer) {
        isTrue(roomState == RoomState.READY, "READY 상태에서만 참여 가능합니다.");
        players.join(joinPlayer);
    }

    public void setMiniGame(List<MiniGame> miniGames) {
        state(miniGames.size() <= 5, "미니게임은 5개 이하여야 합니다.");
        this.miniGames = miniGames;
    }

    // TODO: 플레이어가 방에서 나가는 로직 필요

    public boolean hasNoMiniGames() {
        return miniGames.isEmpty();
    }

    public boolean isInPlayingState() {
        return roomState == RoomState.PLAYING;
    }

    public boolean hasEnoughPlayers() {
        return players.playerCount() >= MINIMUM_GUEST_COUNT && players.playerCount() <= MAXIMUM_GUEST_COUNT;
    }

    public void setPlaying() {
        roomState = RoomState.PLAYING;
    }

    public boolean isHost(Player player) {
        return host.equals(player);
    }
}

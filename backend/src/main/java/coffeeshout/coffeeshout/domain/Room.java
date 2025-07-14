package coffeeshout.coffeeshout.domain;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.state;

import coffeeshout.coffeeshout.domain.player.Player;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class Room {

    private Long id;

    private JoinCode joinCode;

    private List<Player> players = new ArrayList<>();

    private Roulette roulette;

    private List<MiniGame> miniGames = new ArrayList<>();

    private RoomState roomState;

    public static final int MAXIMUM_GUEST_COUNT = 9;
    public static final int MINIMUM_GUEST_COUNT = 2;

    public Room(JoinCode joinCode, Roulette roulette) {
        this.joinCode = joinCode;
        this.roulette = roulette;
        this.roomState = RoomState.READY;
    }

    public void joinPlayer(Player joinPlayer) {
        isTrue(players.size() < MAXIMUM_GUEST_COUNT, "게임은 최대 9명까지 참여할 수 있습니다.");
        isTrue(players.stream().noneMatch(player -> player.isSameName(joinPlayer)), "이미 존재하는 플레이어 이름입니다.");
        isTrue(roomState == RoomState.READY, "READY 상태에서만 참여 가능합니다.");
        // TODO: 룰렛 확률 조정
        players.add(joinPlayer);
    }

    public void addMiniGame(MiniGame miniGame) {
        state(miniGames.size() < 5, "미니게임은 5개 이하여야 합니다.");

        miniGames.add(miniGame);
    }

    public boolean hasNoMiniGames() {
        return miniGames.isEmpty();
    }

    public boolean isInPlayingState() {
        return roomState == RoomState.PLAYING;
    }

    public boolean hasEnoughPlayers() {
        return players.size() >= MINIMUM_GUEST_COUNT && players.size() <= MAXIMUM_GUEST_COUNT;
    }

    public void setPlaying() {
        roomState = RoomState.PLAYING;
    }
}

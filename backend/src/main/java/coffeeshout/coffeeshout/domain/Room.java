package coffeeshout.coffeeshout.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class Room {

    private Long id;

    private JoinCode joinCode;

    private Player host;

    private List<Player> players = new ArrayList<>();

    private Roulette roulette;

    private List<MiniGame> miniGames;

    private RoomState roomState;

    public static final int MAXIMUM_PLAYER_COUNT = 9;
    public static final int MINIMUM_PLAYER_COUNT = 2;

    Room(JoinCode joinCode, Player host, Roulette roulette, List<MiniGame> miniGames) {
        validate(miniGames);
        this.joinCode = joinCode;
        this.roulette = roulette;
        this.host = host;
        this.roomState = RoomState.READY;
    }

    public void playMiniGame() {
        validateMinimumPlayer();
        // TODO: 미니게임 실행시키기
    }

    public void playRoulette() {
        validateMinimumPlayer();
        if (roomState != RoomState.PLAYING) {
            throw new IllegalStateException("게임 중일때만 룰렛을 돌릴 수 있습니다.");
        }
        // TODO: 룰렛 실행시키기
    }

    public void joinPlayer(Player joinPlayer) {
        if (totalPlayerCount() == MAXIMUM_PLAYER_COUNT) {
            throw new IllegalArgumentException("게임은 최대 9명까지 참여할 수 있습니다.");
        }
        if (players.stream().anyMatch(player -> player.isSameName(joinPlayer))) {
            throw new IllegalArgumentException("이미 존재하는 플레이어 이름입니다.");
        }
        players.add(joinPlayer);
    }

    private void validate(List<MiniGame> miniGames) {
        if (miniGames.isEmpty()) {
            throw new IllegalArgumentException("미니게임은 한 개 이상이어야 합니다.");
        }
    }

    private void validateMinimumPlayer() {
        if (totalPlayerCount() < MINIMUM_PLAYER_COUNT) {
            throw new IllegalStateException("게임을 시작 하려면 플레이어가 2명 이상이어야 합니다.");
        }
    }

    private Integer totalPlayerCount() {
        return players.size() + 1;
    }
}

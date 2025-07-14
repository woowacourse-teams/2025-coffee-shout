package coffeeshout.coffeeshout.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class Room {

    private Long id;

    private JoinCode joinCode;

    private List<Player> players = new ArrayList<>();

    private Roulette roulette;

    private List<MiniGame> miniGames;

    private RoomState roomState;

    Room(JoinCode joinCode, Roulette roulette, List<MiniGame> miniGames) {
        validate(miniGames);
        this.joinCode = joinCode;
        this.roulette = roulette;
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

    private void validate(List<MiniGame> miniGames) {
        if (miniGames.isEmpty()) {
            throw new IllegalArgumentException("미니게임은 한 개 이상이어야 합니다.");
        }
    }

    private void validateMinimumPlayer() {
        if (players.size() < 2) {
            throw new IllegalStateException("게임을 시작 하려면 플레이어가 2명 이상이어야 합니다.");
        }
    }
}

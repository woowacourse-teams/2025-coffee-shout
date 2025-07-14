package coffeeshout.coffeeshout.domain.player;

import static org.springframework.util.Assert.state;

import coffeeshout.coffeeshout.domain.Menu;
import coffeeshout.coffeeshout.domain.Room;

public class Host extends Player {

    public Host(final Long id, final String name, final Menu menu, final Room room) {
        super(id, name, menu, room);
    }

    public void playMiniGame() {
        state(room.hasEnoughPlayers(), "미니게임은 2~9명의 플레이어가 참여해야 시작할 수 있습니다.");
        state(!room.hasNoMiniGames(), "미니게임은 1개 이상 선택해야 합니다.");
        room.setPlaying();
        // TODO: 미니게임 실행시키기
    }

    public void playRoulette() {
        state(room.hasEnoughPlayers(), "룰렛은 2~9명의 플레이어가 참여해야 시작할 수 있습니다.");
        state(room.isInPlayingState(), "게임 중일때만 룰렛을 돌릴 수 있습니다.");
        room.setPlaying();
        // TODO: 룰렛 실행시키기
    }
}

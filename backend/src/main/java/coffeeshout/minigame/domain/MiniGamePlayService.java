package coffeeshout.minigame.domain;

import static org.springframework.util.Assert.state;

import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;

public class MiniGamePlayService {

    public void playMiniGame(Player player, Room room) {
        state(player.equals(room.getHost()), "미니게임은 호스트만 시작할 수 있습니다.");
        state(!room.hasNoMiniGames(), "미니게임은 1개 이상 선택해야 합니다.");
        // TODO: 미니게임 실행시키기
    }
}

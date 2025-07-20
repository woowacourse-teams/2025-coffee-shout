package coffeeshout.minigame.domain;

import static org.springframework.util.Assert.state;

import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.RouletteRoom;

public class MiniGamePlayService {

    public void playMiniGame(Player player, RouletteRoom room) {
        state(player.equals(room.getHost()), "미니게임은 호스트만 시작할 수 있습니다.");
        // TODO: 미니게임 실행시키기
    }
}

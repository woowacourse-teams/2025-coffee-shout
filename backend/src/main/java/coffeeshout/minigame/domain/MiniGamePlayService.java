package coffeeshout.minigame.domain;

import static org.springframework.util.Assert.state;

import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.Room;

public class MiniGamePlayService {

    public void playMiniGame(Player player, Room room) {
        state(player.equals(room.getHost()), "미니게임은 호스트만 시작할 수 있습니다.");
        state(room.hasEnoughPlayers(), "미니게임은 2~9명의 플레이어가 참여해야 시작할 수 있습니다.");
        state(!room.hasNoMiniGames(), "미니게임은 1개 이상 선택해야 합니다.");
        // TODO: 미니게임 실행시키기
    }
}

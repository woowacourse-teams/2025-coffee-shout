package coffeeshout.coffeeshout.domain.service;

import static org.springframework.util.Assert.state;

import coffeeshout.coffeeshout.domain.Room;
import coffeeshout.coffeeshout.domain.player.Player;

public class RoulettePlayService {

    public void playRoulette(Player host, Room room) {
        state(room.isHost(host), "룰렛은 호스트만 시작할 수 있습니다.");
        state(room.hasEnoughPlayers(), "룰렛은 2~9명의 플레이어가 참여해야 시작할 수 있습니다.");
        state(room.isInPlayingState(), "게임 중일때만 룰렛을 돌릴 수 있습니다.");
        room.setPlaying();
        // TODO: setPlaying 이름 바꾸기, 여기에 예외처리 넣기
        // TODO: 룰렛 실행시키기
    }
}

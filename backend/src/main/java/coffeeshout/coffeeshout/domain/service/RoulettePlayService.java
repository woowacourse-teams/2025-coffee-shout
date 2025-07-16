package coffeeshout.coffeeshout.domain.service;

import static org.springframework.util.Assert.state;

import coffeeshout.coffeeshout.domain.Room;
import coffeeshout.coffeeshout.domain.player.Player;

public class RoulettePlayService {

    public Player playRoulette(Player host, Room room) {
        state(room.isHost(host), "룰렛은 호스트만 시작할 수 있습니다.");
        return room.startRoulette();
        // TODO: setPlaying 이름 바꾸기, 여기에 예외처리 넣기
        // TODO: 룰렛 실행시키기
    }
}

package coffeeshout.room.domain;

import static org.springframework.util.Assert.state;

import coffeeshout.player.domain.Player;

public class RoulettePlayService {

    public Player playRoulette(Player host, Room room) {
        state(room.isHost(host), "룰렛은 호스트만 시작할 수 있습니다.");
        return room.startRoulette();
    }
}

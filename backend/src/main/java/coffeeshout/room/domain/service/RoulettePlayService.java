package coffeeshout.room.domain.service;

import static org.springframework.util.Assert.state;

import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;

public class RoulettePlayService {

    public Player playRoulette(Player host, Room room) {
        state(room.isHost(host), "룰렛은 호스트만 시작할 수 있습니다.");
        return room.startRoulette();
    }
}

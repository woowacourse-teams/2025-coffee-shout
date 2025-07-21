package coffeeshout.room.domain;

import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.repository.RoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hibernate.mapping.Join;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomFinder {

    private final RoomRepository roomRepository;

    public Room findById(Long roomId) {
        Player player1 = new Player("꾹이");
        Player player2 = new Player("한스");
        Player player3 = new Player("루키");
        Player player4 = new Player("엠제이");

        Room room = new Room(new JoinCode("ABCDE"), new Roulette(new JavaRandomGenerator()), player1);
        room.joinPlayer(player2);
        room.joinPlayer(player3);
        room.joinPlayer(player4);
        return room;
//        return roomRepository.findById(roomId)
//                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다."));
    }
}

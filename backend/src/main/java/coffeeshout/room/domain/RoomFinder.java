package coffeeshout.room.domain;

import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.repository.RoomRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomFinder {

    private final RoomRepository roomRepository;

    public Room findByJoinCode(JoinCode joinCode) {
        return roomRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new NoSuchElementException("해당하는 초대 코드의 방이 존재하지 않습니다."));
    }
}

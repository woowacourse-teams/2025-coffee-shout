package coffeeshout.room.domain.service;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.repository.RoomRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomQueryService {

    private final RoomRepository roomRepository;

    public Room findByJoinCode(JoinCode joinCode) {
        return roomRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new NoSuchElementException("방이 존재하지 않습니다."));
    }

    public boolean existsByJoinCode(JoinCode joinCode) {
        return roomRepository.existsByJoinCode(joinCode);
    }
}

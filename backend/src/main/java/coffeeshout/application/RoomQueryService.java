package coffeeshout.application;

import coffeeshout.domain.Room;
import coffeeshout.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomQueryService {

    private final RoomRepository roomRepository;

    public Room findById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다."));
    }
}

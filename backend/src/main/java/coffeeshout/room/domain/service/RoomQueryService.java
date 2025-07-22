package coffeeshout.room.domain.service;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomQueryService {

    private final RoomRepository roomRepository;

    public Room findById(Long roomId) {
        return roomRepository.findByIdWithAssociations(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다."));
    }

    public Room findByJoinCode(JoinCode joinCode) {
        return roomRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다."));
    }
}

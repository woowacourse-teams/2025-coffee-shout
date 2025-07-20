package coffeeshout.room.domain.service;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.RouletteRoom;
import coffeeshout.room.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomFinder {

    private final RoomRepository roomRepository;

    public RouletteRoom findById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다."));
    }

    public RouletteRoom findByJoinCode(JoinCode joinCode) {
        return roomRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("방이 존재하지 않습니다."));
    }
}

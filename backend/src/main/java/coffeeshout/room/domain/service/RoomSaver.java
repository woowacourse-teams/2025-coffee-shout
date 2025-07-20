package coffeeshout.room.domain.service;

import coffeeshout.room.domain.RouletteRoom;
import coffeeshout.room.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomSaver {

    private final RoomRepository roomRepository;

    public RouletteRoom save(RouletteRoom room) {
        return roomRepository.save(room);
    }
}

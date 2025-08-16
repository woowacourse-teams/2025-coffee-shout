package coffeeshout.room.domain.service;

import static org.springframework.util.Assert.isTrue;

import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomCommandService {

    private final RoomRepository roomRepository;

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public void delete(Room room) {
        isTrue(room.getJoinCode() != null, "JoinCode가 존재하지 않습니다.");

        roomRepository.deleteByJoinCode(room.getJoinCode());
    }
}

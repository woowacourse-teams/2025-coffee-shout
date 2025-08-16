package coffeeshout.room.domain.service;

import static org.springframework.util.Assert.notNull;

import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.repository.RoomRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomCommandService {

    private final RoomRepository roomRepository;

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public void delete(@NonNull Room room) {
        notNull(room.getJoinCode(), "JoinCode가 존재하지 않습니다.");

        roomRepository.deleteByJoinCode(room.getJoinCode());
    }
}

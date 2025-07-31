package coffeeshout.room.domain.service;

import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.repository.RoomRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomCommandService {

    private final RoomRepository roomRepository;
    private final TaskScheduler taskScheduler;

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public void delayCleanUp(Room room, Duration delay) {
        taskScheduler.schedule(() -> roomRepository.deleteByJoinCode(room.getJoinCode()),
                Instant.now().plus(Duration.ofSeconds(1)));
    }
}

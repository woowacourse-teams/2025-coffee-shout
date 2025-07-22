package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.Room;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RoomRepository extends Repository<Room, Long> {

    Optional<Room> findById(Long roomId);
}

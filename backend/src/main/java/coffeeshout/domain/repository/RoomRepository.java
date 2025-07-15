package coffeeshout.domain.repository;

import coffeeshout.domain.Room;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RoomRepository extends Repository<Long, Room> {

    Optional<Room> findById(Long roomId);
}

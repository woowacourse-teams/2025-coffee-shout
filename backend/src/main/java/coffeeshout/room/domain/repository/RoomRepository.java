package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RoomRepository extends Repository<Long, Room> {

    Optional<Room> findById(Long roomId);

    boolean existsByJoinCode(JoinCode joinCode);

    Room save(Room room);
}

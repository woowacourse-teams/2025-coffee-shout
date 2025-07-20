package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.RouletteRoom;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RoomRepository extends Repository<RouletteRoom, Long> {

    Optional<RouletteRoom> findById(Long roomId);

    Optional<RouletteRoom> findByJoinCode(JoinCode joinCode);

    boolean existsByJoinCode(JoinCode joinCode);

    RouletteRoom save(RouletteRoom room);
}

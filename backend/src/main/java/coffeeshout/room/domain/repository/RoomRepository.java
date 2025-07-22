package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface RoomRepository extends Repository<Room, Long> {

    Optional<Room> findById(Long roomId);

    @Query("SELECT r FROM Room r " +
            "LEFT JOIN FETCH r.players p " +
            "LEFT JOIN FETCH p.menu " +
            "WHERE r.id = :id")
    Optional<Room> findByIdWithAssociations(Long id);

    Optional<Room> findByJoinCode(JoinCode joinCode);

    boolean existsByJoinCode(JoinCode joinCode);

    Room save(Room room);
}

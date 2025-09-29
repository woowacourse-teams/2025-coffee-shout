package coffeeshout.room.infra.persistance;

import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RoomJpaRepository extends Repository<RoomEntity, Long> {
    RoomEntity save(RoomEntity roomEntity);

    Optional<RoomEntity> findByJoinCode(String joinCode);

    void deleteAll();
}

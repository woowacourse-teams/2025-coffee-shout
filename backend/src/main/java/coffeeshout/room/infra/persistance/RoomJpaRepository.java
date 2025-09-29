package coffeeshout.room.infra.persistance;

import org.springframework.data.repository.Repository;

public interface RoomJpaRepository extends Repository<RoomEntity, Long> {

    RoomEntity save(RoomEntity roomEntity);
}

package coffeeshout.room.infra.persistance;

import org.springframework.data.repository.Repository;

public interface PlayerJpaRepository extends Repository<PlayerEntity, Long> {

    PlayerEntity save(PlayerEntity playerEntity);
}

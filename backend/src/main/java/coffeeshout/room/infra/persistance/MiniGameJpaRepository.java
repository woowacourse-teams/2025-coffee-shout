package coffeeshout.room.infra.persistance;

import org.springframework.data.repository.Repository;

public interface MiniGameJpaRepository extends Repository<MiniGameEntity, Long> {

    MiniGameEntity save(MiniGameEntity miniGameEntity);
}

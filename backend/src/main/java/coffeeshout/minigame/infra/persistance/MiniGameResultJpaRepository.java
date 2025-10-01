package coffeeshout.minigame.infra.persistance;

import org.springframework.data.repository.Repository;

public interface MiniGameResultJpaRepository extends Repository<MiniGameResultEntity, Long> {

    MiniGameResultEntity save(MiniGameResultEntity miniGameResultEntity);

    void deleteAll();
}

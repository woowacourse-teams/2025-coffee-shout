package coffeeshout.minigame.cardgame.infra.persistence;

import org.springframework.data.repository.Repository;

public interface MiniGameResultJpaRepository extends Repository<MiniGameResultEntity, Long> {

    MiniGameResultEntity save(MiniGameResultEntity miniGameResultEntity);
}

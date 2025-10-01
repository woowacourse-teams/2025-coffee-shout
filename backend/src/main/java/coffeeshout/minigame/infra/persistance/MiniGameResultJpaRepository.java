package coffeeshout.minigame.infra.persistance;

import coffeeshout.minigame.infra.persistance.MiniGameEntity;
import coffeeshout.room.infra.persistance.PlayerEntity;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface MiniGameResultJpaRepository extends Repository<MiniGameResultEntity, Long> {
    
    MiniGameResultEntity save(MiniGameResultEntity miniGameResultEntity);
    
    List<MiniGameResultEntity> findByMiniGamePlay(MiniGameEntity miniGamePlay);
    
    List<MiniGameResultEntity> findByPlayer(PlayerEntity player);
}

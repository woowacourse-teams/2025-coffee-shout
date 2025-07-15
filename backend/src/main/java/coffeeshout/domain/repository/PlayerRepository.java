package coffeeshout.domain.repository;

import coffeeshout.domain.Player;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface PlayerRepository extends Repository<Long, Player> {

    Optional<Player> findById(Long playerId);
}

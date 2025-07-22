package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.player.Menu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface MenuRepository extends Repository<Menu, Long> {

    Optional<Menu> findById(Long menuId);

    List<Menu> findAll();
}

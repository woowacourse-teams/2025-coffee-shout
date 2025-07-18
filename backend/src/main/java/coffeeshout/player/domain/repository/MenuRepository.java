package coffeeshout.player.domain.repository;

import coffeeshout.player.domain.Menu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface MenuRepository extends Repository<Menu, Long> {

    Optional<Menu> findById(Long menuId);

    List<Menu> findAll();
}

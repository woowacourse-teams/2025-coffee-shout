package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.menu.Menu;
import java.util.List;
import java.util.Optional;

public interface MenuRepository {

    Optional<Menu> findById(Long menuId);

    List<Menu> findAll();

    Menu save(Menu menu);
}

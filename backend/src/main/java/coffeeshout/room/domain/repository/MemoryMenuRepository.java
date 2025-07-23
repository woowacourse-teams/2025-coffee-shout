package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.player.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryMenuRepository implements MenuRepository {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    private final Map<Long, Menu> menus;

    public MemoryMenuRepository() {
        this.menus = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<Menu> findById(Long menuId) {
        return Optional.ofNullable(menus.get(menuId));
    }

    @Override
    public List<Menu> findAll() {
        return new ArrayList<>(menus.values());
    }

    @Override
    public Menu save(Menu menu) {
        menu.setId(ID_GENERATOR.getAndIncrement());
        menus.put(menu.getId(), menu);
        return menus.get(menu.getId());
    }
}

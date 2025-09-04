package coffeeshout.room.application;

import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.menu.TemperatureAvailability;
import coffeeshout.room.domain.service.MenuQueryService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuQueryService menuQueryService;

    public List<Menu> getAll() {
        return menuQueryService.getAll();
    }

    public List<Menu> getAllMenuByCategoryId(Long categoryId) {
        return menuQueryService.getAllByCategoryId(categoryId);
    }

    public Menu getMenu(Long menuId, String customName) {
        Optional<Menu> menu = menuQueryService.findById(menuId);
        return menu.orElseGet(() -> new Menu(0L, customName, null, TemperatureAvailability.BOTH));
    }
}

package coffeeshout.room.application;

import coffeeshout.room.domain.menu.MenuCategory;
import coffeeshout.room.domain.menu.ProvidedMenu;
import coffeeshout.room.domain.service.MenuCategoryQueryService;
import coffeeshout.room.domain.service.MenuQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuApplicationService {

    private final MenuQueryService menuQueryService;
    private final MenuCategoryQueryService menuCategoryQueryService;

    public List<ProvidedMenu> getAllMenus() {
        return menuQueryService.getAll();
    }

    public List<ProvidedMenu> getMenusByCategory(Long categoryId) {
        return menuQueryService.getAllByCategoryId(categoryId);
    }

    public List<MenuCategory> getAllCategories() {
        return menuCategoryQueryService.getAll();
    }
}

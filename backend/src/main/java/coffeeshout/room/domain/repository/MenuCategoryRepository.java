package coffeeshout.room.domain.repository;

import coffeeshout.room.domain.menu.MenuCategory;
import java.util.List;

public interface MenuCategoryRepository {

    List<MenuCategory> getAll();

    MenuCategory save(MenuCategory category);
}

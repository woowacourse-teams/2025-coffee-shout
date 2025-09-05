package coffeeshout.fixture;

import coffeeshout.room.domain.menu.MenuCategory;

public class MenuCategoryFixture {

    public static MenuCategory 커피() {
        return new MenuCategory(1L, "커피", "커피.jpg");
    }

    public static MenuCategory 에이드() {
        return new MenuCategory(3L, "에이드", "에이드.jpg");
    }
}

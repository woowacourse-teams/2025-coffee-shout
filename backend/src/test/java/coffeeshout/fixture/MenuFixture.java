package coffeeshout.fixture;

import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.menu.MenuCategory;
import coffeeshout.room.domain.menu.TemperatureAvailability;
import org.springframework.test.util.ReflectionTestUtils;

public class MenuFixture {

    public static Menu 아메리카노() {
        final Menu 아메리카노 = new Menu("아메리카노", 1L, TemperatureAvailability.BOTH);
        ReflectionTestUtils.setField(아메리카노, "id", 1L);
        return 아메리카노;
    }

    public static Menu 라떼() {
        final Menu 라떼 = new Menu("라떼", 1L, TemperatureAvailability.BOTH);
        ReflectionTestUtils.setField(라떼, "id", 2L);
        return 라떼;
    }

    public static Menu 아이스티() {
        final Menu 아이스티 = new Menu("아이스티", 3L, TemperatureAvailability.BOTH);
        ReflectionTestUtils.setField(아이스티, "id", 3L);
        return 아이스티;
    }
}

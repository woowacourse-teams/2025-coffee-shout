package coffeeshout.room.application;

import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.service.MenuCommandService;
import org.springframework.stereotype.Component;

@Component
public class MenuInitializer {

    private final MenuCommandService menuCommandService;

    public MenuInitializer(MenuCommandService menuCommandService) {
        this.menuCommandService = menuCommandService;
        init();
    }

    private void init() {
        menuCommandService.save(new Menu("아메리카노", "americano.jpg"));
        menuCommandService.save(new Menu("카페라떼", "latte.jpg"));
        menuCommandService.save(new Menu("카푸치노", "cappuccino.jpg"));
        menuCommandService.save(new Menu("에스프레소", "espresso.jpg"));
        menuCommandService.save(new Menu("프라푸치노", "frappuccino.jpg"));
    }
}

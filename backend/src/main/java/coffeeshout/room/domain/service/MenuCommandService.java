package coffeeshout.room.domain.service;

import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.repository.MenuRepository;
import org.springframework.stereotype.Service;

@Service
public class MenuCommandService {

    private final MenuRepository menuRepository;

    public MenuCommandService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public void save(Menu menu) {
        menuRepository.save(menu);
    }
}

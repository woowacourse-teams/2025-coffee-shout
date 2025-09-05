package coffeeshout.room.domain.service;

import coffeeshout.room.domain.menu.ProvidedMenu;
import coffeeshout.room.domain.repository.MenuRepository;
import org.springframework.stereotype.Service;

@Service
public class MenuCommandService {

    private final MenuRepository menuRepository;

    public MenuCommandService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public void save(ProvidedMenu menu) {
        menuRepository.save(menu);
    }
}

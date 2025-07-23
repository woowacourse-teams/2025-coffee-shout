package coffeeshout.room.domain.service;

import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.repository.MenuRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MenuQueryService {

    private final MenuRepository menuRepository;

    public MenuQueryService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public Menu findById(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 존재하지 않습니다."));
    }

    public List<Menu> findAll() {
        return menuRepository.findAll();
    }
}

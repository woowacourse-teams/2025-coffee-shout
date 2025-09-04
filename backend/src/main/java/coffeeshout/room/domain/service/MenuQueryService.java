package coffeeshout.room.domain.service;

import coffeeshout.global.exception.GlobalErrorCode;
import coffeeshout.global.exception.custom.NotExistElementException;
import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.repository.MenuRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MenuQueryService {

    private final MenuRepository menuRepository;

    public MenuQueryService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public Menu getById(Long menuId) {
        return findById(menuId)
                .orElseThrow(() -> new NotExistElementException(GlobalErrorCode.NOT_EXIST, "메뉴가 존재하지 않습니다."));
    }

    public Optional<Menu> findById(Long menuId) {
        return menuRepository.findById(menuId);
    }

    public List<Menu> getAll() {
        return menuRepository.findAll();
    }

    public List<Menu> getAllByCategoryId(Long categoryId) {
        return menuRepository.findAll().stream()
                .filter(menu -> Objects.equals(menu.getMenuCategoryId(), categoryId))
                .toList();
    }
}

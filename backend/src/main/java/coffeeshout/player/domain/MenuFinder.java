package coffeeshout.player.domain;

import coffeeshout.player.domain.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuFinder {

    private final MenuRepository menuRepository;

    public Menu findById(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴가 존재하지 않습니다."));
    }
}

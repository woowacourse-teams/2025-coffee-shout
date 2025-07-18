package coffeeshout.player.application;

import coffeeshout.player.domain.Menu;
import coffeeshout.player.domain.MenuFinder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuFinder menuFinder;

    public List<Menu> getAll() {
        return menuFinder.findAll();
    }
}

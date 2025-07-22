package coffeeshout.room.application;

import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.MenuFinder;
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

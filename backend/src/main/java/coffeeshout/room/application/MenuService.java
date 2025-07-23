package coffeeshout.room.application;

import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.service.MenuQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuQueryService menuQueryService;

    public List<Menu> getAll() {
        return menuQueryService.findAll();
    }
}

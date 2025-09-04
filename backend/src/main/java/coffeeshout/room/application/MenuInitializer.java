package coffeeshout.room.application;

import coffeeshout.room.domain.menu.Menu;
import coffeeshout.room.domain.menu.TemperatureAvailability;
import coffeeshout.room.domain.service.MenuCategoryCommandService;
import coffeeshout.room.domain.service.MenuCommandService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class MenuInitializer {

    private final MenuCommandService menuCommandService;

    public MenuInitializer(MenuCommandService menuCommandService) {
        this.menuCommandService = menuCommandService;
    }

    @PostConstruct
    private void init() throws IOException {
        final Yaml yaml = new Yaml();
        final InputStream inputStream = new ClassPathResource("data/menu-data.yml").getInputStream();
        final MenuDtos menuDtos = yaml.loadAs(inputStream, MenuDtos.class);

        menuDtos.getMenus().forEach(item -> menuCommandService.save(new Menu(
                item.getId(),
                item.getName(),
                item.getCategoryId(),
                TemperatureAvailability.from(item.getTemperatureAvailability())
        )));
    }

    @Setter
    @Getter
    protected static class MenuDtos {
        private List<MenuDto> menus;
    }

    @Setter
    @Getter
    protected static class MenuDto {
        private Long id;
        private String name;
        private Long categoryId;
        private String temperatureAvailability;
    }
}

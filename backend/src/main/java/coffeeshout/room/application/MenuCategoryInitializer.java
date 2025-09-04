package coffeeshout.room.application;

import coffeeshout.room.domain.menu.MenuCategory;
import coffeeshout.room.domain.service.MenuCategoryCommandService;
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
public class MenuCategoryInitializer {

    private final MenuCategoryCommandService menuCategoryCommandService;

    public MenuCategoryInitializer(MenuCategoryCommandService menuCategoryCommandService) {
        this.menuCategoryCommandService = menuCategoryCommandService;
    }

    @PostConstruct
    private void init() throws IOException {
        final Yaml yaml = new Yaml();
        final InputStream inputStream = new ClassPathResource("data/menu-category-data.yml").getInputStream();
        final MenuCategoryDtos menuCategoryDtos = yaml.loadAs(inputStream, MenuCategoryDtos.class);

        menuCategoryDtos.getMenuCategories().forEach(item -> menuCategoryCommandService.save(new MenuCategory(
                item.getId(),
                item.getName()
        )));
    }

    @Setter
    @Getter
    protected static class MenuCategoryDtos {
        private List<MenuCategoryDto> menuCategories;
    }

    @Setter
    @Getter
    protected static class MenuCategoryDto {
        private Long id;
        private String name;
    }
}

package coffeeshout.room.application;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.fixture.MenuCategoryFixture;
import coffeeshout.fixture.MenuFixture;
import coffeeshout.global.ServiceTest;
import coffeeshout.room.domain.menu.MenuCategory;
import coffeeshout.room.domain.menu.ProvidedMenu;
import coffeeshout.room.domain.repository.MenuCategoryRepository;
import coffeeshout.room.domain.repository.MenuRepository;
import coffeeshout.room.domain.service.MenuCategoryQueryService;
import coffeeshout.room.domain.service.MenuQueryService;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

class MenuApplicationServiceTest extends ServiceTest {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Autowired
    private MenuQueryService menuQueryService;

    @Autowired
    private MenuCategoryQueryService menuCategoryQueryService;

    @Autowired
    private MenuApplicationService menuApplicationService;

    private ProvidedMenu 아메리카노;
    private ProvidedMenu 라떼;
    private ProvidedMenu 아이스티;
    private MenuCategory 커피;
    private MenuCategory 티;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(menuRepository, "menus", new ConcurrentHashMap<>());
        ReflectionTestUtils.setField(menuRepository, "idGenerator", new AtomicLong(1));
        ReflectionTestUtils.setField(menuCategoryRepository, "menuCategories", new ConcurrentHashMap<>());
        ReflectionTestUtils.setField(menuCategoryRepository, "idGenerator", new AtomicLong(1));

        커피 = menuCategoryRepository.save(MenuCategoryFixture.커피());
        티 = menuCategoryRepository.save(MenuCategoryFixture.티());
        아메리카노 = menuRepository.save((ProvidedMenu) MenuFixture.아메리카노());
        라떼 = menuRepository.save((ProvidedMenu) MenuFixture.라떼());
        아이스티 = menuRepository.save((ProvidedMenu) MenuFixture.아이스티());
    }

    @Test
    @DisplayName("모든 메뉴를 조회한다")
    void 모든_메뉴를_조회한다() {
        // given

        // when
        List<ProvidedMenu> result = menuApplicationService.getAllMenus();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(아메리카노, 라떼, 아이스티);
    }

    @Test
    @DisplayName("카테고리 ID로 해당 카테고리의 메뉴들을 조회한다")
    void 카테고리_ID로_해당_카테고리의_메뉴들을_조회한다() {
        // given
        Long categoryId = 1L;

        // when
        List<ProvidedMenu> result = menuApplicationService.getMenusByCategory(categoryId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(아메리카노, 라떼);
    }

    @Test
    @DisplayName("모든 메뉴 카테고리를 조회한다")
    void 모든_메뉴_카테고리를_조회한다() {
        // given

        // when
        List<MenuCategory> result = menuApplicationService.getAllCategories();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(커피, 티);
    }
}

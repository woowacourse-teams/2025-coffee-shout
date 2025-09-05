package coffeeshout.room.domain.menu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomMenuTest {

    @Test
    @DisplayName("CustomMenu를 생성한다")
    void CustomMenu를_생성한다() {
        // given
        String name = "custom coffee";
        String categoryImageUrl = "custom.jpg";

        // when
        CustomMenu customMenu = new CustomMenu(name, categoryImageUrl);

        // then
        assertThat(customMenu.getName()).isEqualTo(name);
        assertThat(customMenu.getCategoryImageUrl()).isEqualTo(categoryImageUrl);
        assertThat(customMenu.getTemperatureAvailability()).isEqualTo(TemperatureAvailability.BOTH);
    }

    @Test
    @DisplayName("CustomMenu의 ID는 항상 0이다")
    void CustomMenu의_ID는_항상_0이다() {
        // given
        CustomMenu customMenu = new CustomMenu("custom coffee", "custom.jpg");

        // when
        Long id = customMenu.getId();

        // then
        assertThat(id).isEqualTo(0L);
    }

    @Test
    @DisplayName("CustomMenu는 카테고리 이미지 URL을 직접 관리한다")
    void CustomMenu는_카테고리_이미지_URL을_직접_관리한다() {
        // given
        String categoryImageUrl = "my-custom.jpg";
        CustomMenu customMenu = new CustomMenu("custom menu", categoryImageUrl);

        // when
        String result = customMenu.getCategoryImageUrl();

        // then
        assertThat(result).isEqualTo(categoryImageUrl);
    }
}
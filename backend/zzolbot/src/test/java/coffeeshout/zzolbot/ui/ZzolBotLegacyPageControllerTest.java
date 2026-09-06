package coffeeshout.zzolbot.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ZzolBotLegacyPageController")
class ZzolBotLegacyPageControllerTest {

    @Test
    void zzolbot_뷰_이름을_반환한다() {
        // 이 뷰는 레거시 백오피스만 쓴다. ZzolBotChatController 에 남겨 두면
        // 거기 붙은 /admin/api/zzolbot 경로에서도 HTML 이 나간다.
        assertThat(new ZzolBotLegacyPageController().page()).isEqualTo("admin/zzolbot");
    }
}

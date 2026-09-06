package coffeeshout.zzolbot.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 레거시 Thymeleaf 백오피스의 ZzolBot 화면.
 *
 * <p>{@link ZzolBotChatController}에서 떼어냈다. 그쪽은 경로를 둘 잡는데
 * ({@code /admin/zzolbot}, {@code /admin/api/zzolbot}) 메서드 매핑은 클래스 경로 전부에
 * 붙으므로, 뷰 반환 메서드를 그대로 두면 {@code GET /admin/api/zzolbot}도 HTML 을
 * 돌려준다. JSON 만 오갈 경로에서 HTML 이 나오면 SPA 쪽 오류가 엉뚱하게 읽힌다.
 *
 * <p>이 클래스는 Phase 5 에서 템플릿과 함께 통째로 지운다.
 */
@Controller
public class ZzolBotLegacyPageController {

    @GetMapping("/admin/zzolbot")
    public String page() {
        return "admin/zzolbot";
    }
}

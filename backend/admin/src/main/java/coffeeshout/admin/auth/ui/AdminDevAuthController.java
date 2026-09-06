package coffeeshout.admin.auth.ui;

import coffeeshout.admin.auth.application.AdminAuthService;
import coffeeshout.admin.auth.ui.request.AdminDevLoginRequest;
import coffeeshout.admin.auth.ui.response.AdminTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로컬 개발용 로그인. 구글 클라이언트 설정 없이 백오피스를 띄울 수 있게 한다.
 *
 * <p>{@code @Profile("local")}이라 배포 환경에는 <b>빈 자체가 등록되지 않는다.</b>
 * 시큐리티 설정도 local 에서만 이 경로를 공개하므로 두 겹으로 잠긴다.
 * 허용목록 검사는 그대로 통과해야 하므로 아무 이메일이나 되는 것은 아니다.
 */
@RestController
@RequestMapping("/admin/api/auth")
@RequiredArgsConstructor
@Profile("local")
public class AdminDevAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/dev-login")
    public AdminTokenResponse devLogin(@Valid @RequestBody AdminDevLoginRequest request) {
        return new AdminTokenResponse(adminAuthService.devLogin(request.email()));
    }
}

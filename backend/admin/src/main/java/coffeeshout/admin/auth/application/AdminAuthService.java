package coffeeshout.admin.auth.application;

import coffeeshout.admin.account.application.AdminAccountService;
import coffeeshout.admin.account.domain.AdminAccountErrorCode;
import coffeeshout.admin.account.domain.AdminEmail;
import coffeeshout.admin.auth.domain.AdminTokenIssuer;
import coffeeshout.admin.auth.domain.SocialIdTokenVerifier;
import coffeeshout.global.exception.custom.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 관리자 로그인.
 *
 * <p>순서가 중요하다. <b>토큰 검증 → 허용목록 대조 → 토큰 발급</b>이다.
 * 허용목록을 먼저 보면 이메일을 주장하기만 해도 목록에 있는지 알아낼 수 있어,
 * 관리자 이메일을 찾는 탐색 경로가 열린다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final SocialIdTokenVerifier socialIdTokenVerifier;
    private final AdminAccountService adminAccountService;
    private final AdminTokenIssuer adminTokenIssuer;

    public String login(String idToken) {
        return issueForAllowed(socialIdTokenVerifier.verifyAndExtractEmail(idToken));
    }

    /**
     * local 프로필 전용. 구글 검증 없이 허용목록만으로 토큰을 발급한다.
     *
     * <p>이 경로는 컨트롤러가 {@code @Profile("local")}로 막고, 시큐리티 설정도 local 에서만
     * 공개 경로로 등록한다. 서버 프로필과 컨트롤러 등록 두 겹으로 잠근다.
     */
    public String devLogin(String rawEmail) {
        log.warn("dev-login 으로 관리자 토큰 발급 시도: email={}", rawEmail);
        return issueForAllowed(rawEmail);
    }

    private String issueForAllowed(String rawEmail) {
        // 형식이 어긋난 값도 목록에 없는 값과 같은 응답을 준다. 둘을 구분해 주면
        // 어떤 이메일이 형식만 맞는지 훑어 관리자 계정을 좁혀 갈 수 있다.
        final AdminEmail email = AdminEmail.parse(rawEmail)
                .filter(adminAccountService::isAllowed)
                .orElseThrow(() -> new BusinessException(
                        AdminAccountErrorCode.NOT_ADMIN, "관리자 허용목록에 없는 계정입니다."));
        return adminTokenIssuer.issue(email);
    }
}

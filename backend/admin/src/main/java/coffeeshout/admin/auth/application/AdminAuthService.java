package coffeeshout.admin.auth.application;

import coffeeshout.admin.account.application.AdminAccountService;
import coffeeshout.admin.account.domain.AdminAccountErrorCode;
import coffeeshout.admin.account.domain.AdminEmails;
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
        final String email = socialIdTokenVerifier.verifyAndExtractEmail(idToken);
        return issueForAllowed(email);
    }

    /**
     * local 프로필 전용. 구글 검증 없이 허용목록만으로 토큰을 발급한다.
     *
     * <p>이 경로는 컨트롤러가 {@code @Profile("local")}로 막고, 시큐리티 설정도 local 에서만
     * 공개 경로로 등록한다. 서버 프로필과 컨트롤러 등록 두 겹으로 잠근다.
     */
    public String devLogin(String email) {
        log.warn("dev-login 으로 관리자 토큰 발급: email={}", AdminEmails.normalize(email));
        return issueForAllowed(email);
    }

    private String issueForAllowed(String email) {
        final String normalized = AdminEmails.normalize(email);
        if (!adminAccountService.isAllowed(normalized)) {
            // 목록에 없는 것과 형식이 틀린 것을 같은 메시지로 돌려준다.
            throw new BusinessException(
                    AdminAccountErrorCode.NOT_ADMIN, "관리자 허용목록에 없는 계정입니다.");
        }
        return adminTokenIssuer.issue(normalized);
    }
}

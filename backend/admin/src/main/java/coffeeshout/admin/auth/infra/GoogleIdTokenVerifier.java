package coffeeshout.admin.auth.infra;

import coffeeshout.admin.account.domain.AdminAccountErrorCode;
import coffeeshout.admin.auth.AdminAuthProperties;
import coffeeshout.admin.auth.domain.SocialIdTokenVerifier;
import coffeeshout.global.exception.custom.BusinessException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

/**
 * 구글 ID 토큰 검증.
 *
 * <p>{@link JwtDecoders#fromIssuerLocation}이 구글 OpenID 디스커버리 문서를 읽어
 * JWKS 주소를 얻고 서명 키를 캐싱한다. 서명(RS256), 발급자, 만료는 디코더가 처리하므로
 * 여기서는 <b>대상(aud)</b>과 <b>이메일 검증 여부</b>만 추가로 확인한다.
 *
 * <p>이메일 검증(email_verified)을 반드시 본다. 이메일 소유가 확인되지 않은 계정이
 * 관리자 이메일과 같은 주소를 주장하면 허용목록 대조가 그대로 뚫린다.
 */
@Slf4j
@Component
public class GoogleIdTokenVerifier implements SocialIdTokenVerifier {

    private static final String ISSUER = "https://accounts.google.com";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_EMAIL_VERIFIED = "email_verified";

    private final JwtDecoder jwtDecoder;
    private final String clientId;

    public GoogleIdTokenVerifier(AdminAuthProperties properties) {
        this(JwtDecoders.fromIssuerLocation(ISSUER), properties.googleClientId());
    }

    GoogleIdTokenVerifier(JwtDecoder jwtDecoder, String clientId) {
        this.jwtDecoder = jwtDecoder;
        this.clientId = clientId;
    }

    @Override
    public String verifyAndExtractEmail(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new BusinessException(
                    AdminAccountErrorCode.GOOGLE_ID_TOKEN_INVALID, "ID 토큰이 비어 있습니다.");
        }

        final Jwt jwt = decode(idToken);
        validateAudience(jwt);
        validateEmailVerified(jwt);

        final String email = jwt.getClaimAsString(CLAIM_EMAIL);
        if (email == null || email.isBlank()) {
            throw new BusinessException(
                    AdminAccountErrorCode.GOOGLE_ID_TOKEN_INVALID, "ID 토큰에 이메일이 없습니다.");
        }
        return email;
    }

    private Jwt decode(String idToken) {
        try {
            return jwtDecoder.decode(idToken);
        } catch (JwtException e) {
            // 실패 사유를 응답에 담지 않는다. 어떤 검증에서 걸렸는지 알려주면 탐색을 돕는다.
            log.warn("구글 ID 토큰 검증 실패: {}", e.getMessage());
            throw new BusinessException(
                    AdminAccountErrorCode.GOOGLE_ID_TOKEN_INVALID, "구글 인증에 실패했습니다.");
        }
    }

    private void validateAudience(Jwt jwt) {
        final List<String> audience = jwt.getClaimAsStringList(JwtClaimNames.AUD);
        if (audience == null || !audience.contains(clientId)) {
            // 다른 서비스용으로 발급된 구글 토큰을 여기 들고 오는 경로를 막는다.
            log.warn("구글 ID 토큰 대상 불일치: aud={}", audience);
            throw new BusinessException(
                    AdminAccountErrorCode.GOOGLE_ID_TOKEN_INVALID, "구글 인증에 실패했습니다.");
        }
    }

    private void validateEmailVerified(Jwt jwt) {
        // 구글은 이 클레임을 boolean 으로도 문자열로도 보낸 이력이 있어 둘 다 받는다.
        final Object verified = jwt.getClaim(CLAIM_EMAIL_VERIFIED);
        final boolean isVerified = Boolean.TRUE.equals(verified) || "true".equals(verified);
        if (!isVerified) {
            throw new BusinessException(
                    AdminAccountErrorCode.GOOGLE_EMAIL_UNVERIFIED,
                    "이메일이 검증되지 않은 구글 계정입니다.");
        }
    }
}

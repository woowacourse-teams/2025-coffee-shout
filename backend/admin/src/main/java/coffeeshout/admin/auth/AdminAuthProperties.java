package coffeeshout.admin.auth;

import coffeeshout.admin.account.domain.AdminEmails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 관리자 인증 설정.
 *
 * <p>{@code emails}는 환경변수 {@code ADMIN_EMAILS}로 주입하는 부트스트랩 허용목록이다.
 * DB {@code admin_account} 테이블과 합집합으로 판정하되, 이 목록은 백오피스 UI에서 삭제할 수 없다.
 * 관리자가 실수로 자기들을 전부 지워 아무도 못 들어가는 상태를 막는 break-glass 경로다.
 */
@Validated
@ConfigurationProperties(prefix = "admin.auth")
public record AdminAuthProperties(
        List<String> emails,
        String googleClientId,
        @NotBlank
        @Size(min = 32, message = "관리자 JWT secret은 HS256 최소 키 길이(32자) 이상이어야 합니다.")
        String jwtSecret,
        @Positive long tokenValiditySeconds
) {

    public AdminAuthProperties {
        emails = normalizeAll(emails);
    }

    public boolean isBootstrap(String email) {
        final String normalized = AdminEmails.normalize(email);
        return normalized != null && emails.contains(normalized);
    }

    public List<String> bootstrapEmails() {
        return emails;
    }

    private static List<String> normalizeAll(List<String> raw) {
        if (raw == null) {
            return List.of();
        }
        // 순서를 유지해야 목록 화면에서 부트스트랩 관리자가 매번 같은 자리에 보인다.
        final Set<String> normalized = new LinkedHashSet<>();
        for (String each : raw) {
            final String value = AdminEmails.normalize(each);
            if (value != null) {
                normalized.add(value);
            }
        }
        return List.copyOf(normalized);
    }
}

package coffeeshout.admin.account.domain;

import coffeeshout.global.exception.custom.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UI로 추가한 관리자. 환경변수 부트스트랩 관리자는 이 테이블에 없다.
 */
@Entity
@Table(name = "admin_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** 누가 이 관리자를 추가했는지. 시스템이 넣은 경우 null. */
    @Column(name = "created_by_email", length = 255)
    private String createdByEmail;

    @Column(nullable = false)
    private Instant createdAt;

    public static AdminAccount create(String email, String createdByEmail, Instant now) {
        final String normalized = AdminEmails.normalize(email);
        validateEmail(normalized);

        final AdminAccount account = new AdminAccount();
        account.email = normalized;
        account.createdByEmail = AdminEmails.normalize(createdByEmail);
        account.createdAt = now;
        return account;
    }

    private static void validateEmail(String normalized) {
        // 정규화 결과가 null이면 원본이 null이거나 공백뿐이었다는 뜻이다.
        if (normalized == null) {
            throw new BusinessException(
                    AdminAccountErrorCode.INVALID_ADMIN_EMAIL, "관리자 이메일은 비어 있을 수 없습니다.");
        }
        if (normalized.length() > 255) {
            throw new BusinessException(
                    AdminAccountErrorCode.INVALID_ADMIN_EMAIL, "관리자 이메일이 255자를 넘습니다.");
        }
        // 완전한 RFC 검증은 하지 않는다. 실제 소유 검증은 구글 ID 토큰이 하므로
        // 여기서는 목록에 넣을 수 없는 값(@ 없음)만 거른다.
        final int at = normalized.indexOf('@');
        if (at <= 0 || at == normalized.length() - 1 || normalized.indexOf('@', at + 1) >= 0) {
            throw new BusinessException(
                    AdminAccountErrorCode.INVALID_ADMIN_EMAIL, "관리자 이메일 형식이 올바르지 않습니다.");
        }
    }
}

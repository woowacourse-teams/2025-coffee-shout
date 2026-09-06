package coffeeshout.admin.auth.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.admin.auth.AdminAuthProperties;
import coffeeshout.global.exception.custom.BusinessException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("JjwtAdminTokenIssuer")
class JjwtAdminTokenIssuerTest {

    private static final String SECRET = "admin-test-secret-key-must-be-at-least-32-bytes";
    private static final String OTHER_SECRET = "another-secret-key-that-is-also-32-bytes-long!!";
    private static final Instant NOW = Instant.parse("2026-09-06T00:00:00Z");
    private static final long VALIDITY_SECONDS = 3600;

    private static JjwtAdminTokenIssuer issuerAt(Instant now) {
        return new JjwtAdminTokenIssuer(
                new AdminAuthProperties(List.of(), "client-id", SECRET, VALIDITY_SECONDS),
                Clock.fixed(now, ZoneOffset.UTC));
    }

    private final JjwtAdminTokenIssuer issuer = issuerAt(NOW);

    @Nested
    class issue {

        @Test
        void 발급한_토큰을_다시_검증하면_같은_주체가_나온다() {
            final String token = issuer.issue("mj@zzol.site");

            assertThat(issuer.verify(token).email()).isEqualTo("mj@zzol.site");
        }

        @Test
        void 만료_직전까지는_유효하다() {
            final String token = issuer.issue("mj@zzol.site");
            final JjwtAdminTokenIssuer justBeforeExpiry =
                    issuerAt(NOW.plusSeconds(VALIDITY_SECONDS - 1));

            assertThat(justBeforeExpiry.verify(token).email()).isEqualTo("mj@zzol.site");
        }
    }

    @Nested
    class verify {

        @Test
        void 만료된_토큰은_거부한다() {
            final String token = issuer.issue("mj@zzol.site");
            final JjwtAdminTokenIssuer afterExpiry =
                    issuerAt(NOW.plus(Duration.ofSeconds(VALIDITY_SECONDS + 60)));

            assertThatThrownBy(() -> afterExpiry.verify(token))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("만료된 관리자 토큰");
        }

        @Test
        void 다른_키로_서명된_토큰은_거부한다() {
            final String forged = Jwts.builder()
                    .subject("attacker@evil.site")
                    .claim("type", "ADMIN")
                    .expiration(new Date(NOW.toEpochMilli() + 600_000))
                    .signWith(Keys.hmacShaKeyFor(OTHER_SECRET.getBytes(StandardCharsets.UTF_8)))
                    .compact();

            assertThatThrownBy(() -> issuer.verify(forged))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("유효하지 않은 관리자 토큰");
        }

        @Test
        void type이_ADMIN이_아니면_거부한다() {
            // 관리자 secret 이 사용자 JWT secret 으로 폴백될 수 있어, 같은 키로 서명된
            // 사용자 토큰이 관리자 권한으로 통과하면 안 된다.
            final String userToken = Jwts.builder()
                    .subject("42")
                    .claim("userCode", "abc")
                    .expiration(new Date(NOW.toEpochMilli() + 600_000))
                    .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                    .compact();

            assertThatThrownBy(() -> issuer.verify(userToken))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("관리자 토큰이 아닙니다");
        }

        @Test
        void 주체가_없으면_거부한다() {
            final String noSubject = Jwts.builder()
                    .claim("type", "ADMIN")
                    .expiration(new Date(NOW.toEpochMilli() + 600_000))
                    .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                    .compact();

            assertThatThrownBy(() -> issuer.verify(noSubject))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("주체가 없는");
        }

        @Test
        void 형식이_아닌_문자열은_거부한다() {
            assertThatThrownBy(() -> issuer.verify("not-a-jwt"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("유효하지 않은 관리자 토큰");
        }
    }
}

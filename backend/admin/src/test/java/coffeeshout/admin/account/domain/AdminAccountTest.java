package coffeeshout.admin.account.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.global.exception.custom.BusinessException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("AdminAccount")
class AdminAccountTest {

    private static final Instant NOW = Instant.parse("2026-09-06T00:00:00Z");

    @Nested
    class create {

        @Test
        void 이메일을_소문자로_정규화해_저장한다() {
            final AdminAccount account = AdminAccount.create("  MJ@Zzol.Site  ", "root@zzol.site", NOW);

            assertThat(account.getEmail()).isEqualTo("mj@zzol.site");
        }

        @Test
        void 추가한_관리자_이메일도_정규화한다() {
            final AdminAccount account = AdminAccount.create("mj@zzol.site", " ROOT@Zzol.site ", NOW);

            assertThat(account.getCreatedByEmail()).isEqualTo("root@zzol.site");
        }

        @Test
        void 시스템이_추가한_경우_추가자가_없다() {
            final AdminAccount account = AdminAccount.create("mj@zzol.site", null, NOW);

            assertThat(account.getCreatedByEmail()).isNull();
        }

        @Test
        void 생성_시각은_주입받은_값을_쓴다() {
            final AdminAccount account = AdminAccount.create("mj@zzol.site", null, NOW);

            assertThat(account.getCreatedAt()).isEqualTo(NOW);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void 비어있는_이메일은_거부한다(String email) {
            assertThatThrownBy(() -> AdminAccount.create(email, null, NOW))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("비어 있을 수 없습니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"nobody", "@zzol.site", "mj@", "mj@a@b.site"})
        void 형식이_아닌_이메일은_거부한다(String email) {
            assertThatThrownBy(() -> AdminAccount.create(email, null, NOW))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("형식이 올바르지 않습니다");
        }

        @Test
        void 이메일이_255자를_넘으면_거부한다() {
            final String tooLong = "a".repeat(250) + "@zzol.site";

            assertThatThrownBy(() -> AdminAccount.create(tooLong, null, NOW))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("255자");
        }

        @Test
        void 경계값_255자는_허용한다() {
            final String exact = "a".repeat(255 - "@zzol.site".length()) + "@zzol.site";

            assertThatCode(() -> AdminAccount.create(exact, null, NOW)).doesNotThrowAnyException();
        }
    }
}

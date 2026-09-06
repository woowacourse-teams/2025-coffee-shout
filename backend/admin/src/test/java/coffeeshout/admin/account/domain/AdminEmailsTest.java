package coffeeshout.admin.account.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("AdminEmails")
class AdminEmailsTest {

    @Nested
    class normalize {

        @ParameterizedTest
        @CsvSource({
                "MJ@ZZOL.SITE, mj@zzol.site",
                "'  mj@zzol.site  ', mj@zzol.site",
                "'\tMj@Zzol.Site\n', mj@zzol.site",
                "mj@zzol.site, mj@zzol.site"
        })
        void 공백을_제거하고_소문자로_낮춘다(String raw, String expected) {
            assertThat(AdminEmails.normalize(raw)).isEqualTo(expected);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        void 값이_없으면_null을_돌려준다(String raw) {
            assertThat(AdminEmails.normalize(raw)).isNull();
        }
    }

    @Nested
    class isNormalized {

        @Test
        void 이미_정규화된_값이면_참이다() {
            assertThat(AdminEmails.isNormalized("mj@zzol.site")).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"MJ@zzol.site", " mj@zzol.site", "mj@zzol.site "})
        void 정규화되지_않은_값이면_거짓이다(String raw) {
            assertThat(AdminEmails.isNormalized(raw)).isFalse();
        }

        @Test
        void null은_거짓이다() {
            assertThat(AdminEmails.isNormalized(null)).isFalse();
        }
    }
}

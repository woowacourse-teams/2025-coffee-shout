package coffeeshout.admin.quality.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import coffeeshout.admin.quality.domain.NicknameAuditQuality;
import coffeeshout.admin.quality.domain.QualityStatisticsRepository;
import coffeeshout.admin.quality.domain.ReportSla;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("QualityService")
@ExtendWith(MockitoExtension.class)
class QualityServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-06T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private QualityStatisticsRepository qualityStatisticsRepository;

    private QualityService service() {
        return new QualityService(qualityStatisticsRepository, CLOCK);
    }

    private void givenDurations(List<Long> minutes) {
        given(qualityStatisticsRepository.findResolvedDurationMinutes(any(), any()))
                .willReturn(minutes);
        given(qualityStatisticsRepository.findOldestPendingReportCreatedAt())
                .willReturn(Optional.empty());
    }

    @Nested
    class reportSla {

        @Test
        void 중앙값과_p95를_nearest_rank로_고른다() {
            // 1..10 이면 p50 은 5번째(5), p95 는 10번째(10)다.
            givenDurations(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));

            final ReportSla sla = service().reportSla(30);

            assertThat(sla.p50Minutes()).isEqualTo(5);
            assertThat(sla.p95Minutes()).isEqualTo(10);
        }

        @Test
        void 이상치_하나가_중앙값을_끌고_가지_않는다() {
            // 평균이면 1440 짜리 하나가 전체를 왜곡한다. 중앙값은 버틴다.
            givenDurations(List.of(1L, 2L, 3L, 1440L));

            assertThat(service().reportSla(30).p50Minutes()).isEqualTo(2);
        }

        @Test
        void 표본이_하나면_두_백분위가_같다() {
            givenDurations(List.of(7L));

            final ReportSla sla = service().reportSla(30);

            assertThat(sla.p50Minutes()).isEqualTo(7);
            assertThat(sla.p95Minutes()).isEqualTo(7);
        }

        @Test
        void 처리된_신고가_없으면_0이다() {
            givenDurations(List.of());

            final ReportSla sla = service().reportSla(30);

            assertThat(sla.resolvedCount()).isZero();
            assertThat(sla.p50Minutes()).isZero();
            assertThat(sla.p95Minutes()).isZero();
        }

        @Test
        void 가장_오래된_미처리_신고의_나이를_분으로_돌려준다() {
            given(qualityStatisticsRepository.findResolvedDurationMinutes(any(), any()))
                    .willReturn(List.of());
            given(qualityStatisticsRepository.findOldestPendingReportCreatedAt())
                    .willReturn(Optional.of(NOW.minusSeconds(3 * 3600)));
            given(qualityStatisticsRepository.countPendingReports()).willReturn(4L);

            final ReportSla sla = service().reportSla(30);

            assertThat(sla.oldestPendingMinutes()).isEqualTo(180);
            assertThat(sla.pendingCount()).isEqualTo(4L);
        }

        @Test
        void 미처리_신고가_없으면_대기_시간이_0이다() {
            givenDurations(List.of());

            assertThat(service().reportSla(30).oldestPendingMinutes()).isZero();
        }
    }

    @Nested
    class nicknameAuditQuality {

        @Test
        void 조회_기간을_시계_기준으로_계산한다() {
            given(qualityStatisticsRepository.findNicknameAuditQuality(
                    NOW.minusSeconds(30 * 24 * 3600), NOW))
                    .willReturn(new NicknameAuditQuality(100, 10, 5));

            assertThat(service().nicknameAuditQuality(30).total()).isEqualTo(100);
        }
    }
}

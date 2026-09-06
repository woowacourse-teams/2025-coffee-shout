package coffeeshout.admin.quality.application;

import coffeeshout.admin.quality.domain.NicknameAuditQuality;
import coffeeshout.admin.quality.domain.QualityStatisticsRepository;
import coffeeshout.admin.quality.domain.ReportSla;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 운영 품질 지표.
 *
 * <p>"일이 얼마나 밀렸나"(대기 큐)와 "잘하고 있나"(여기)는 다른 질문이다.
 * 대기 건수가 0이어도 검열 모델이 멀쩡한 닉네임을 계속 막고 있을 수 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QualityService {

    private final QualityStatisticsRepository qualityStatisticsRepository;
    private final Clock clock;

    public NicknameAuditQuality nicknameAuditQuality(int days) {
        final Instant to = clock.instant();
        return qualityStatisticsRepository.findNicknameAuditQuality(to.minus(Duration.ofDays(days)), to);
    }

    public ReportSla reportSla(int days) {
        final Instant now = clock.instant();
        final List<Long> durations =
                qualityStatisticsRepository.findResolvedDurationMinutes(now.minus(Duration.ofDays(days)), now);

        final long pendingCount = qualityStatisticsRepository.countPendingReports();
        final long oldestPendingMinutes = qualityStatisticsRepository
                .findOldestPendingReportCreatedAt()
                .map(createdAt -> Duration.between(createdAt, now).toMinutes())
                .orElse(0L);

        return new ReportSla(
                durations.size(),
                percentile(durations, 50),
                percentile(durations, 95),
                pendingCount,
                oldestPendingMinutes);
    }

    /**
     * 정렬된 목록에서 백분위 값을 고른다(nearest-rank).
     *
     * <p>보간하지 않는다. 표본이 몇 건뿐인 백오피스에서 보간값은 실제로 없었던 소요 시간을
     * 만들어 낸다. "실제 있었던 신고 중 하나"를 고르는 편이 읽는 사람에게 정직하다.
     */
    private static long percentile(List<Long> sortedAscending, int percentile) {
        if (sortedAscending.isEmpty()) {
            return 0;
        }
        final int rank = (int) Math.ceil(percentile / 100.0 * sortedAscending.size());
        return sortedAscending.get(Math.min(rank, sortedAscending.size()) - 1);
    }
}

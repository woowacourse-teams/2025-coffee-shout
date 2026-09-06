package coffeeshout.admin.quality.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QualityStatisticsRepository {

    NicknameAuditQuality findNicknameAuditQuality(Instant from, Instant to);

    /** 기간 내 처리 완료된 신고들의 소요 시간(분). 백분위는 서비스가 계산한다. */
    List<Long> findResolvedDurationMinutes(Instant from, Instant to);

    long countPendingReports();

    /** 가장 오래된 미처리 신고의 접수 시각. 없으면 비어 있다. */
    Optional<Instant> findOldestPendingReportCreatedAt();
}

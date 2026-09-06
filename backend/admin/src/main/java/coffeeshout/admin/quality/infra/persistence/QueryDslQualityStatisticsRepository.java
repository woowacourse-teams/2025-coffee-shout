package coffeeshout.admin.quality.infra.persistence;

import coffeeshout.admin.quality.domain.NicknameAuditQuality;
import coffeeshout.admin.quality.domain.QualityStatisticsRepository;
import coffeeshout.profanity.domain.audit.NicknameFeedback.OperatorDecision;
import coffeeshout.profanity.domain.audit.QNicknameFeedback;
import coffeeshout.report.domain.ReportStatus;
import coffeeshout.report.infra.persistence.QReport;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QueryDslQualityStatisticsRepository implements QualityStatisticsRepository {

    private static final QNicknameFeedback FEEDBACK = QNicknameFeedback.nicknameFeedback;
    private static final QReport REPORT = QReport.report;

    private final JPAQueryFactory queryFactory;

    @Override
    public NicknameAuditQuality findNicknameAuditQuality(Instant from, Instant to) {
        // AI 판정과 관리자 결정의 네 조합을 한 번에 집계한다. 조합마다 쿼리를 돌리면
        // 네 번 왕복하면서도 서로 다른 시점의 데이터를 섞어 합이 안 맞을 수 있다.
        final List<Tuple> rows = queryFactory
                .select(FEEDBACK.aiFlagged, FEEDBACK.operatorDecision, FEEDBACK.count())
                .from(FEEDBACK)
                .where(FEEDBACK.createdAt.goe(from), FEEDBACK.createdAt.lt(to))
                .groupBy(FEEDBACK.aiFlagged, FEEDBACK.operatorDecision)
                .fetch();

        long total = 0;
        long falsePositive = 0;
        long falseNegative = 0;
        for (Tuple row : rows) {
            final boolean aiFlagged = Boolean.TRUE.equals(row.get(FEEDBACK.aiFlagged));
            final OperatorDecision decision = row.get(FEEDBACK.operatorDecision);
            final long count = nullToZero(row.get(FEEDBACK.count()));

            total += count;
            if (aiFlagged && decision == OperatorDecision.ALLOWED) {
                falsePositive += count;
            }
            if (!aiFlagged && decision == OperatorDecision.BLOCKED) {
                falseNegative += count;
            }
        }
        return new NicknameAuditQuality(total, falsePositive, falseNegative);
    }

    @Override
    public List<Long> findResolvedDurationMinutes(Instant from, Instant to) {
        // 소요 시간 계산을 DB 함수로 밀지 않는다. TIMESTAMPDIFF 는 방언에 묶여
        // 테스트 DB 와 운영 DB 가 다르면 조용히 갈라진다. 두 시각을 받아 자바에서 뺀다.
        return queryFactory
                .select(REPORT.createdAt, REPORT.resolvedAt)
                .from(REPORT)
                .where(REPORT.resolvedAt.isNotNull(), REPORT.resolvedAt.goe(from), REPORT.resolvedAt.lt(to))
                .fetch()
                .stream()
                .map(row -> Duration.between(row.get(REPORT.createdAt), row.get(REPORT.resolvedAt))
                        .toMinutes())
                .sorted()
                .toList();
    }

    @Override
    public long countPendingReports() {
        return nullToZero(queryFactory
                .select(REPORT.count())
                .from(REPORT)
                .where(REPORT.status.eq(ReportStatus.PENDING))
                .fetchOne());
    }

    @Override
    public Optional<Instant> findOldestPendingReportCreatedAt() {
        return Optional.ofNullable(queryFactory
                .select(REPORT.createdAt.min())
                .from(REPORT)
                .where(REPORT.status.eq(ReportStatus.PENDING))
                .fetchOne());
    }

    private static long nullToZero(Long value) {
        return value == null ? 0L : value;
    }
}

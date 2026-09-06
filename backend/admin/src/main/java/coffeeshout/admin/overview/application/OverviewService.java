package coffeeshout.admin.overview.application;

import coffeeshout.admin.ipblock.IpBlockAdminService;
import coffeeshout.admin.overview.domain.DailyTrendPoint;
import coffeeshout.admin.overview.domain.GamePlayStat;
import coffeeshout.admin.overview.domain.OverviewStatisticsRepository;
import coffeeshout.admin.overview.domain.OverviewStatisticsRepository.GamePlayCount;
import coffeeshout.admin.overview.domain.RoomFunnel;
import coffeeshout.profanity.application.ProfanityAuditService;
import coffeeshout.profanity.domain.audit.NicknameAuditStatus;
import coffeeshout.report.application.ReportAdminService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈 대시보드.
 *
 * <p>운영자가 로그인해서 3초 안에 두 가지에 답할 수 있어야 한다.
 * "지금 처리할 일이 있나", "서비스가 잘 돌고 있나".
 *
 * <p>Grafana 와 겹치는 지표는 담지 않는다. 응답시간, 에러율, JVM 은 그쪽이 이미 본다.
 * 두 곳이 다른 숫자를 말하는 순간 양쪽 다 신뢰를 잃는다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OverviewService {

    private final OverviewStatisticsRepository overviewStatisticsRepository;
    private final ReportAdminService reportAdminService;
    private final ProfanityAuditService profanityAuditService;
    private final IpBlockAdminService ipBlockAdminService;
    private final Clock clock;

    /** 상단의 처리 대기 큐. 0 이면 오늘 볼 게 없다는 뜻이다. */
    public ActionQueue actionQueue() {
        return new ActionQueue(
                reportAdminService.countPending(),
                countAudits(NicknameAuditStatus.FLAGGED),
                countAudits(NicknameAuditStatus.PENDING),
                ipBlockAdminService.getBlockedIps().size());
    }

    /**
     * 개수만 필요한데 목록 API 로 센다. {@code :profanity} 모듈에 count 메서드를 새로 내는 대신
     * 기존 조회를 재사용해 이 작업이 다른 모듈로 번지지 않게 했다. Page 가 내부적으로
     * count 쿼리를 따로 돌리므로 전체를 읽어 오지는 않는다.
     */
    private long countAudits(NicknameAuditStatus status) {
        return profanityAuditService.listByStatus(status, PageRequest.of(0, 1)).getTotalElements();
    }

    /** 오늘(KST 자정 기준)의 숫자와 퍼널. */
    public DailySummary today() {
        final LocalDate today = LocalDate.now(clock);
        return summaryOf(today);
    }

    public DailySummary summaryOf(LocalDate date) {
        final LocalDateTime from = date.atStartOfDay();
        // 상한을 exclusive 로 둔다. 자정 정각에 생긴 방이 이틀에 걸쳐 두 번 세지지 않게 한다.
        final LocalDateTime to = date.plusDays(1).atStartOfDay();

        // 가입만 Instant 컬럼이라 같은 경계를 시간대에 맞춰 변환한다.
        final ZoneId zone = clock.getZone();

        return new DailySummary(
                date,
                overviewStatisticsRepository.findFunnelBetween(from, to),
                overviewStatisticsRepository.countPlayersBetween(from, to),
                overviewStatisticsRepository.countSignupsBetween(
                        from.atZone(zone).toInstant(), to.atZone(zone).toInstant()));
    }

    /**
     * 최근 N일 흐름. 값이 없는 날을 0으로 채워 돌려준다.
     *
     * <p>빈 날을 빼면 차트가 날짜를 건너뛰어 "이틀 조용했다"가 안 보인다.
     * 오히려 그 조용한 날이 봐야 할 신호다.
     */
    public List<DailyTrendPoint> trend(int days) {
        final LocalDate today = LocalDate.now(clock);
        final LocalDate start = today.minusDays(days - 1L);

        final Map<LocalDate, DailyTrendPoint> found = overviewStatisticsRepository
                .findDailyTrend(start.atStartOfDay(), today.plusDays(1).atStartOfDay())
                .stream()
                .collect(Collectors.toMap(DailyTrendPoint::date, point -> point));

        return IntStream.range(0, days)
                .mapToObj(start::plusDays)
                .map(date -> found.getOrDefault(date, new DailyTrendPoint(date, 0, 0, 0)))
                .toList();
    }

    /** 게임별 완료 수와 비중. 비중이 0에 가까운 게임은 아무도 고르지 않는다는 뜻이다. */
    public List<GamePlayStat> gamePlayStats(int days) {
        final LocalDate today = LocalDate.now(clock);
        final List<GamePlayCount> counts = overviewStatisticsRepository.countPlaysByGame(
                today.minusDays(days - 1L).atStartOfDay(), today.plusDays(1).atStartOfDay());

        final long total = counts.stream().mapToLong(GamePlayCount::plays).sum();
        return counts.stream()
                .sorted(Comparator.comparingLong(GamePlayCount::plays).reversed())
                .map(count -> new GamePlayStat(
                        count.miniGameType(),
                        count.plays(),
                        total == 0 ? 0 : (double) count.plays() / total))
                .toList();
    }

    public record ActionQueue(
            long pendingReports,
            long flaggedNicknames,
            long pendingNicknames,
            int blockedIps
    ) {

        /** 하나라도 0이 아니면 화면 상단에 강조한다. */
        public boolean hasWork() {
            return pendingReports > 0 || flaggedNicknames > 0
                    || pendingNicknames > 0 || blockedIps > 0;
        }
    }

    public record DailySummary(
            LocalDate date,
            RoomFunnel funnel,
            long players,
            long signups
    ) {
    }
}

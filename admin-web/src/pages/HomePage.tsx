import { MessageSquareWarning, ShieldBan, SpellCheck } from 'lucide-react';
import { Link } from 'react-router-dom';
import {
  useActionQueue,
  useAuditLogs,
  useDailySummary,
  useGamePlayStats,
  useNicknameAuditQuality,
  useReportSla,
  useTrend,
} from '@/api/queries';
import { lazy, Suspense } from 'react';
import { ActivityFeed } from '@/components/ActivityFeed';
import { FunnelBar } from '@/components/FunnelBar';
import { GameShareList } from '@/components/GameShareList';
import { QueueCard } from '@/components/QueueCard';
import { TrendLegend } from '@/components/TrendLegend';
import { Button } from '@/components/ui/Button';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { ErrorState, Skeleton } from '@/components/ui/EmptyState';
import { PageHeader, Section } from '@/components/ui/PageHeader';
import { formatDurationMinutes, formatNumber, formatPercent } from '@/lib/format';

/**
 * recharts 는 이 차트 하나에만 쓰이는데 gzip 108KB 를 더한다. 지연 로드하면 로그인 화면과
 * 목록 화면이 그 무게를 지지 않는다. 홈은 차트가 조금 늦게 떠도 나머지가 먼저 보인다.
 *
 * <p>범례를 {@link TrendLegend} 로 떼어낸 것이 이 분리의 전제다. 같은 모듈에서 범례를
 * 정적으로 가져오면 recharts 가 메인 청크로 따라 들어와 지연 로드가 무효가 된다.
 */
const TrendChart = lazy(() =>
  import('@/components/TrendChart').then((module) => ({ default: module.TrendChart })),
);

/**
 * 홈. 운영자가 로그인해서 <b>3초 안에</b> 세 가지에 답할 수 있어야 한다.
 * "지금 처리할 일이 있나", "서비스가 평소만큼 돌고 있나", "누가 방금 뭘 바꿨나".
 *
 * <p>담는 지표의 기준은 하나다. <b>Grafana 가 못 보는 것.</b> 응답시간, 에러율, JVM 은
 * 그쪽이 이미 본다. 두 곳이 다른 숫자를 말하는 순간 양쪽 다 신뢰를 잃는다.
 * 여기 있는 것은 전부 도메인 조인이거나 우리 DB 에만 있는 기록이다.
 */
export function HomePage() {
  const queue = useActionQueue();
  const summary = useDailySummary();
  const trend = useTrend(14);
  const games = useGamePlayStats(30);
  const sla = useReportSla(30);
  const auditQuality = useNicknameAuditQuality(30);
  const logs = useAuditLogs(6);

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title="홈"
        description="처리할 일과 서비스 흐름. 인프라 지표는 Grafana(status.zzol.site)가 봅니다."
      />

      <Section title="처리 대기" description="숫자를 누르면 해당 화면으로 갑니다.">
        {queue.isError ? (
          <Card>
            <ErrorState message={(queue.error as Error).message} onRetry={() => queue.refetch()} />
          </Card>
        ) : (
          <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
            {queue.isPending
              ? Array.from({ length: 4 }).map((_, index) => (
                  <Skeleton key={index} className="h-[4.5rem] rounded-lg" />
                ))
              : queue.data && (
                  <>
                    <QueueCard
                      label="미처리 신고"
                      count={queue.data.pendingReports}
                      to="/reports"
                      icon={MessageSquareWarning}
                    />
                    <QueueCard
                      label="검열 FLAGGED"
                      count={queue.data.flaggedNicknames}
                      to="/profanity"
                      icon={SpellCheck}
                    />
                    <QueueCard
                      label="검열 PENDING"
                      count={queue.data.pendingNicknames}
                      to="/profanity"
                      icon={SpellCheck}
                    />
                    <QueueCard
                      label="차단 IP"
                      count={queue.data.blockedIps}
                      to="/ip-blocks"
                      icon={ShieldBan}
                    />
                  </>
                )}
          </div>
        )}
      </Section>

      {/* 왼쪽은 "서비스가 어떻게 돌고 있나", 오른쪽은 "오늘 얼마나 됐나".
        * 추이는 가로가 길어야 모양이 보이고 오늘 숫자는 세로로 쌓아야 자릿수가 비교된다. */}
      <div className="grid gap-4 xl:grid-cols-[minmax(0,2fr)_minmax(17rem,1fr)]">
        <Card>
          <CardHeader
            title="최근 14일"
            description="오늘 숫자만으로는 0이 정상인지 알 수 없습니다."
            actions={<TrendLegend />}
          />
          <CardBody>
            {trend.isPending ? (
              <Skeleton className="h-[220px]" />
            ) : trend.isError ? (
              <ErrorState message={(trend.error as Error).message} onRetry={() => trend.refetch()} />
            ) : (
              trend.data && (
                <Suspense fallback={<Skeleton className="h-[220px]" />}>
                  <TrendChart data={trend.data} />
                </Suspense>
              )
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader title="오늘" description={summary.data?.date} />
          {summary.isPending ? (
            <CardBody className="flex flex-col gap-3">
              {Array.from({ length: 4 }).map((_, index) => (
                <Skeleton key={index} className="h-8" />
              ))}
            </CardBody>
          ) : (
            summary.data && (
              <dl className="divide-y divide-border-default">
                <MetricRow label="방 생성" value={summary.data.funnel.created} />
                <MetricRow
                  label="완주"
                  value={summary.data.funnel.completed}
                  suffix={formatPercent(summary.data.funnel.completionRate, 0)}
                />
                <MetricRow
                  label="참여자"
                  value={summary.data.players}
                  hint="여러 방 참여 시 중복"
                />
                <MetricRow label="신규 가입" value={summary.data.signups} />
              </dl>
            )
          )}
        </Card>
      </div>

      <div className="grid gap-4 xl:grid-cols-2">
        <Card>
          <CardHeader
            title="방 진행 퍼널"
            description="오늘 기준. 오른쪽은 앞 단계 대비 전환율과 이탈 수."
          />
          <CardBody>
            {summary.isPending ? (
              <div className="flex flex-col gap-2">
                {Array.from({ length: 5 }).map((_, index) => (
                  <Skeleton key={index} className="h-7" />
                ))}
              </div>
            ) : (
              summary.data && (
                <FunnelBar
                  stages={[
                    { label: '방 생성', count: summary.data.funnel.created },
                    { label: '다른 사람 입장', count: summary.data.funnel.joined },
                    { label: '게임 시작', count: summary.data.funnel.gameStarted },
                    { label: '룰렛 도달', count: summary.data.funnel.rouletteReached },
                    { label: '완주', count: summary.data.funnel.completed },
                  ]}
                />
              )
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader
            title="게임별 플레이"
            description="최근 30일 완료 기준. 비중이 0에 가까우면 아무도 고르지 않는다는 뜻입니다."
          />
          {games.isPending ? (
            <CardBody className="flex flex-col gap-2.5">
              {Array.from({ length: 4 }).map((_, index) => (
                <Skeleton key={index} className="h-4" />
              ))}
            </CardBody>
          ) : (
            games.data && <GameShareList stats={games.data} />
          )}
        </Card>
      </div>

      <div className="grid gap-4 xl:grid-cols-2">
        <Card>
          <CardHeader
            title="운영 품질"
            description="일이 밀렸는지와 잘하고 있는지는 다른 질문입니다."
          />
          <dl className="divide-y divide-border-default">
            <MetricRow
              label="가장 오래 기다린 신고"
              value={sla.data ? formatDurationMinutes(sla.data.oldestPendingMinutes) : '-'}
              hint="접수 후 경과"
            />
            <MetricRow
              label="신고 처리 중앙값"
              value={sla.data ? formatDurationMinutes(sla.data.p50Minutes) : '-'}
              hint={`최근 30일 ${sla.data?.resolvedCount ?? 0}건`}
            />
            <MetricRow
              label="검열 AI 판정 뒤집힘"
              value={auditQuality.data ? formatPercent(auditQuality.data.overrideRate) : '-'}
              hint="높아지면 모델을 손볼 때"
            />
            <MetricRow
              label="검열 오탐 / 미탐"
              value={
                auditQuality.data
                  ? `${auditQuality.data.falsePositive} / ${auditQuality.data.falseNegative}`
                  : '-'
              }
              hint="AI가 잘못 걸렀다 / 놓쳤다"
            />
          </dl>
        </Card>

        <Card>
          <CardHeader
            title="최근 조치"
            description="Grafana 로는 볼 수 없는 기록입니다."
            actions={
              <Button asChild size="sm" variant="ghost">
                <Link to="/audit-logs">전체 보기</Link>
              </Button>
            }
          />
          {logs.isPending ? (
            <CardBody className="flex flex-col gap-3">
              {Array.from({ length: 5 }).map((_, index) => (
                <Skeleton key={index} className="h-8" />
              ))}
            </CardBody>
          ) : logs.isError ? (
            <ErrorState message={(logs.error as Error).message} onRetry={() => logs.refetch()} />
          ) : (
            <ActivityFeed logs={logs.data?.content ?? []} />
          )}
        </Card>
      </div>
    </div>
  );
}

/**
 * 라벨은 왼쪽, 값은 오른쪽. 값을 한 줄에 세로로 맞춰 두면 위아래로 훑을 때
 * 자릿수가 눈으로 비교된다. 카드를 나란히 두면 그 비교가 안 된다.
 */
function MetricRow({
  label,
  value,
  suffix,
  hint,
}: {
  label: string;
  value: number | string;
  suffix?: string;
  hint?: string;
}) {
  return (
    <div className="flex items-center justify-between gap-3 px-4 py-3">
      <dt className="min-w-0">
        <span className="block text-sm text-ink-secondary">{label}</span>
        {hint && <span className="block text-2xs text-ink-muted">{hint}</span>}
      </dt>
      <dd className="flex shrink-0 items-baseline gap-1.5">
        <span className="text-xl font-bold leading-none tracking-metric text-ink">
          {typeof value === 'number' ? formatNumber(value) : value}
        </span>
        {suffix && <span className="text-xs text-ink-muted">{suffix}</span>}
      </dd>
    </div>
  );
}

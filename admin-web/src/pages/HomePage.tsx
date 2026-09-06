import { MessageSquareWarning, ShieldBan, SpellCheck } from 'lucide-react';
import { useActionQueue, useDailySummary } from '@/api/queries';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { ErrorState, Skeleton } from '@/components/ui/EmptyState';
import { PageHeader, Section } from '@/components/ui/PageHeader';
import { FunnelBar } from '@/components/FunnelBar';
import { QueueCard } from '@/components/QueueCard';
import { formatNumber, formatPercent } from '@/lib/format';

/**
 * 홈. 운영자가 로그인해서 <b>3초 안에</b> 두 가지에 답할 수 있어야 한다.
 * "지금 처리할 일이 있나", "서비스가 잘 돌고 있나".
 *
 * <p>레이아웃을 두 단으로 나눈다. 퍼널은 가로로 길어야 읽히고, 오늘의 숫자는 네 개짜리
 * 짧은 목록이라 세로로 쌓는 편이 밀도가 높다. 넷을 나란히 큰 카드로 두면 두 자리 숫자
 * 하나에 400px 짜리 빈 상자를 주게 된다.
 *
 * <p>최대폭을 둔다. 표 화면과 달리 대시보드는 넓어져 봐야 카드 안쪽 여백만 늘어난다.
 * 넓은 모니터에서 시선이 좌우로 흩어지면 3초 안에 훑는 것이 오히려 어려워진다.
 *
 * <p>응답시간, 에러율, JVM 같은 지표는 담지 않는다. Grafana(status.zzol.site)가 이미
 * 본다. 두 곳이 다른 숫자를 말하는 순간 양쪽 다 신뢰를 잃는다.
 */
export function HomePage() {
  const queue = useActionQueue();
  const summary = useDailySummary();

  return (
    <div className="mx-auto flex w-full max-w-[1400px] flex-col gap-6">
      <PageHeader
        title="홈"
        description="처리할 일과 오늘의 흐름. 인프라 지표는 Grafana가 봅니다."
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

      {/* 퍼널이 넓은 쪽, 오늘의 숫자가 좁은 쪽이다. 퍼널은 막대 길이로 읽는 그림이라
       * 가로가 필요하고, 숫자 넷은 세로로 쌓으면 한눈에 들어온다. */}
      <div className="grid gap-4 xl:grid-cols-[minmax(0,2fr)_minmax(18rem,1fr)]">
        <Card>
          <CardHeader
            title="방 진행 퍼널"
            description="막대는 방 생성 대비. 오른쪽은 앞 단계 대비 전환율과 이탈 수."
          />
          <CardBody>
            {summary.isPending ? (
              <div className="flex flex-col gap-2">
                {Array.from({ length: 5 }).map((_, index) => (
                  <Skeleton key={index} className="h-6" />
                ))}
              </div>
            ) : summary.isError ? (
              <ErrorState
                message={(summary.error as Error).message}
                onRetry={() => summary.refetch()}
              />
            ) : (
              summary.data && (
                <FunnelBar
                  stages={[
                    { label: '방 생성', count: summary.data.funnel.created },
                    { label: '2인 이상 입장', count: summary.data.funnel.joined },
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
    </div>
  );
}

/**
 * 라벨은 왼쪽, 숫자는 오른쪽. 숫자를 한 줄에 세로로 맞춰 두면 위아래로 훑을 때
 * 자릿수가 눈으로 비교된다. 카드 넷을 나란히 두면 그 비교가 안 된다.
 */
function MetricRow({
  label,
  value,
  suffix,
  hint,
}: {
  label: string;
  value: number;
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
        <span className="text-2xl font-bold leading-none tracking-[-0.02em] text-ink">
          {formatNumber(value)}
        </span>
        {suffix && <span className="text-xs text-ink-muted">{suffix}</span>}
      </dd>
    </div>
  );
}

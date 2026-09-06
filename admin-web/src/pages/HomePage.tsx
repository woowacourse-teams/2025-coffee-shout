import { MessageSquareWarning, ShieldBan, SpellCheck } from 'lucide-react';
import { useActionQueue, useDailySummary } from '@/api/queries';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { ErrorState, Skeleton } from '@/components/ui/EmptyState';
import { PageHeader, Section } from '@/components/ui/PageHeader';
import { FunnelBar } from '@/components/FunnelBar';
import { QueueCard } from '@/components/QueueCard';
import { StatCard } from '@/components/StatCard';
import { formatPercent } from '@/lib/format';

/**
 * 홈. 운영자가 로그인해서 <b>3초 안에</b> 두 가지에 답할 수 있어야 한다.
 * "지금 처리할 일이 있나", "서비스가 잘 돌고 있나".
 *
 * <p>그래서 순서가 대기 큐 → 퍼널 → 오늘의 숫자다. 볼거리가 아니라 할 일이 먼저다.
 *
 * <p>응답시간, 에러율, JVM 같은 지표는 담지 않는다. Grafana(status.zzol.site)가 이미
 * 본다. 두 곳이 다른 숫자를 말하는 순간 양쪽 다 신뢰를 잃는다.
 */
export function HomePage() {
  const queue = useActionQueue();
  const summary = useDailySummary();

  return (
    <div className="flex flex-col gap-7">
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
          <div className="grid grid-cols-2 gap-3 xl:grid-cols-4">
            {queue.isPending
              ? Array.from({ length: 4 }).map((_, index) => (
                  <Skeleton key={index} className="h-[5.75rem] rounded-lg" />
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
                      label="검열 대기 (FLAGGED)"
                      count={queue.data.flaggedNicknames}
                      to="/profanity"
                      icon={SpellCheck}
                    />
                    <QueueCard
                      label="검열 대기 (PENDING)"
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

      <Section title="오늘">
        {summary.isError ? (
          <Card>
            <ErrorState
              message={(summary.error as Error).message}
              onRetry={() => summary.refetch()}
            />
          </Card>
        ) : summary.isPending ? (
          <div className="grid grid-cols-2 gap-3 xl:grid-cols-4">
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={index} className="h-[6.5rem] rounded-lg" />
            ))}
          </div>
        ) : (
          summary.data && (
            <div className="grid grid-cols-2 gap-3 xl:grid-cols-4">
              <StatCard label="방 생성" value={summary.data.funnel.created} />
              <StatCard
                label="완주"
                value={summary.data.funnel.completed}
                suffix={formatPercent(summary.data.funnel.completionRate, 0)}
                hint="방 생성 대비"
              />
              <StatCard
                label="참여자"
                value={summary.data.players}
                hint="같은 사람이 여러 방에 들어가면 중복 집계"
              />
              <StatCard label="신규 가입" value={summary.data.signups} />
            </div>
          )
        )}
      </Section>

      <Section>
        <Card>
          <CardHeader
            title="방 진행 퍼널"
            description="막대는 방 생성 대비 비율. 오른쪽은 앞 단계 대비 전환율과 이탈 수."
          />
          <CardBody>
            {summary.isPending ? (
              <div className="flex flex-col gap-2">
                {Array.from({ length: 5 }).map((_, index) => (
                  <Skeleton key={index} className="h-6" />
                ))}
              </div>
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
      </Section>
    </div>
  );
}

import { lazy, Suspense, useState } from 'react';
import { useGamePlayStats, usePeriodSummary, useTrend } from '@/api/queries';
import { FunnelBar } from '@/components/FunnelBar';
import { GameShareList } from '@/components/GameShareList';
import { StatCard } from '@/components/StatCard';
import { TrendLegend } from '@/components/TrendLegend';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { ErrorState, Skeleton } from '@/components/ui/EmptyState';
import { PageHeader, Section } from '@/components/ui/PageHeader';
import { cn } from '@/lib/cn';
import { formatPercent } from '@/lib/format';

const TrendChart = lazy(() =>
  import('@/components/TrendChart').then((module) => ({ default: module.TrendChart })),
);

/**
 * 기간 선택지를 넷으로 묶는다.
 *
 * <p>날짜 범위 피커를 두지 않았다. 운영자가 실제로 묻는 것은 "이번 주 어때",
 * "지난달 대비 어때" 수준이고, 임의 구간이 필요한 분석은 어차피 SQL 로 간다.
 * 피커를 두면 잘못 고른 구간(예: 하루)으로 퍼널이 이상해 보이는 일이 생긴다.
 *
 * <p>90일이 상한이다. 서버가 365까지 받지만 화면에서 열어 두지 않는다. 이 집계는
 * 인덱스를 타도 구간이 길수록 스캔이 늘고, 백오피스 한 번 열자고 운영 DB 를
 * 길게 잡을 이유가 없다.
 */
const RANGES = [7, 14, 30, 90] as const;

export function AnalyticsPage() {
  const [days, setDays] = useState<number>(30);

  const period = usePeriodSummary(days);
  const trend = useTrend(Math.min(days, 90));
  const games = useGamePlayStats(days);

  return (
    <div className="mx-auto flex w-full max-w-[1500px] flex-col gap-6">
      <PageHeader
        title="서비스 분석"
        description="사람들이 이 서비스를 실제로 어떻게 쓰는지. 인프라 지표는 Grafana(status.zzol.site)가 봅니다."
        actions={<RangePicker value={days} onChange={setDays} />}
      />

      {period.isError ? (
        <Card>
          <ErrorState message={(period.error as Error).message} onRetry={() => period.refetch()} />
        </Card>
      ) : (
        <Section
          title="기간 합계"
          description={
            period.data
              ? `${period.data.from} ~ ${period.data.to} (${period.data.days}일)`
              : '불러오는 중'
          }
        >
          <div className="grid grid-cols-2 gap-3 xl:grid-cols-4">
            {period.isPending
              ? Array.from({ length: 4 }).map((_, index) => (
                  <Skeleton key={index} className="h-[6.5rem] rounded-lg" />
                ))
              : period.data && (
                  <>
                    <StatCard label="방 생성" value={period.data.funnel.created} />
                    <StatCard
                      label="완주율"
                      value={formatPercent(period.data.funnel.completionRate)}
                      suffix={`${period.data.funnel.completed}건`}
                      hint="생성된 방 중 DONE 까지 간 비율"
                    />
                    <StatCard
                      label="방당 평균 참여자"
                      value={period.data.avgPlayersPerRoom.toFixed(1)}
                      suffix="명"
                      hint="혼자 만들고 아무도 안 온 방도 분모에 포함"
                    />
                    <StatCard
                      label="신규 가입"
                      value={period.data.signups}
                      hint="비회원도 게임은 가능합니다"
                    />
                  </>
                )}
          </div>
        </Section>
      )}

      <Card>
        <CardHeader
          title="일자별 추이"
          description="합계만 보면 어느 날 무슨 일이 있었는지가 사라집니다."
          actions={<TrendLegend />}
        />
        <CardBody>
          {trend.isPending ? (
            <Skeleton className="h-[260px]" />
          ) : trend.isError ? (
            <ErrorState message={(trend.error as Error).message} onRetry={() => trend.refetch()} />
          ) : (
            trend.data && (
              <Suspense fallback={<Skeleton className="h-[260px]" />}>
                <TrendChart data={trend.data} height={260} />
              </Suspense>
            )
          )}
        </CardBody>
      </Card>

      <div className="grid gap-4 xl:grid-cols-2">
        <Card>
          <CardHeader
            title="방 진행 퍼널"
            description="구간 전체를 한 번에 셉니다. 일자별 합이 아니라서 어제 생기고 오늘 끝난 방도 이어집니다."
          />
          <CardBody>
            {period.isPending ? (
              <div className="flex flex-col gap-2">
                {Array.from({ length: 5 }).map((_, index) => (
                  <Skeleton key={index} className="h-7" />
                ))}
              </div>
            ) : (
              period.data && (
                <FunnelBar
                  stages={[
                    { label: '방 생성', count: period.data.funnel.created },
                    { label: '다른 사람 입장', count: period.data.funnel.joined },
                    { label: '게임 시작', count: period.data.funnel.gameStarted },
                    { label: '룰렛 도달', count: period.data.funnel.rouletteReached },
                    { label: '완주', count: period.data.funnel.completed },
                  ]}
                />
              )
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader
            title="게임별 플레이"
            description="완료 기준. 비중이 0에 가까운 게임은 목록에서 뺄지 고민할 때입니다."
          />
          {games.isPending ? (
            <CardBody className="flex flex-col gap-2.5">
              {Array.from({ length: 6 }).map((_, index) => (
                <Skeleton key={index} className="h-4" />
              ))}
            </CardBody>
          ) : games.isError ? (
            <ErrorState message={(games.error as Error).message} onRetry={() => games.refetch()} />
          ) : (
            games.data && <GameShareList stats={games.data} />
          )}
        </Card>
      </div>

      {/* 이 화면이 무엇을 못 보는지 적어 둔다. 지표를 믿으려면 경계를 알아야 하고,
       * 반년 뒤에 이 숫자를 보는 사람은 여기 적힌 것을 다시 알아낼 방법이 없다. */}
      <Card>
        <CardHeader title="이 숫자가 세지 않는 것" />
        <CardBody className="flex flex-col gap-1.5 text-xs text-ink-secondary">
          <p>
            <b className="text-ink">SCORE_BOARD 단계</b>는 메모리에만 있고 DB 로 내려가지 않아
            퍼널에 없습니다. 늘 0으로 찍히는 칸을 두면 나머지 숫자까지 못 믿게 됩니다.
          </p>
          <p>
            <b className="text-ink">시작만 하고 만 게임</b>은 잡히지 않습니다. mini_game_play 는
            게임이 끝날 때 결과와 함께 저장됩니다.
          </p>
          <p>
            <b className="text-ink">참여자</b>는 사람이 아니라 참여 건수입니다. 같은 사람이 방
            셋에 들어가면 3으로 셉니다.
          </p>
        </CardBody>
      </Card>
    </div>
  );
}

/**
 * 기간 선택. 세그먼트 컨트롤 한 줄로 둔다.
 *
 * <p>드롭다운으로 하면 지금 어느 구간을 보고 있는지 알려면 한 번 열어야 한다.
 * 선택지가 넷뿐이라 전부 펼쳐 두는 편이 클릭도 한 번 덜 든다.
 */
function RangePicker({ value, onChange }: { value: number; onChange: (days: number) => void }) {
  return (
    <div
      role="group"
      aria-label="조회 기간"
      className="inline-flex rounded-md border border-border-default bg-surface p-0.5"
    >
      {RANGES.map((days) => (
        <button
          key={days}
          type="button"
          aria-pressed={value === days}
          onClick={() => onChange(days)}
          className={cn(
            'rounded-[0.3125rem] px-3 py-1 text-xs font-medium transition-colors',
            value === days
              ? 'bg-subtle text-ink'
              : 'text-ink-muted hover:text-ink-secondary',
          )}
        >
          {days}일
        </button>
      ))}
    </div>
  );
}

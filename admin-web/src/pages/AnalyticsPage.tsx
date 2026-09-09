import { lazy, Suspense, useMemo, useState } from 'react';
import { useGamePlayStats, usePeriodSummary, useTrend } from '@/api/queries';
import type { DailyTrend } from '@/api/types';
import { FunnelBar } from '@/components/FunnelBar';
import { GameShareList } from '@/components/GameShareList';
import { Tile } from '@/components/ui/Tile';
import { TileGrid, TileSkeletons } from '@/components/ui/TileGrid';
import { TrendLegend } from '@/components/TrendLegend';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { Skeleton } from '@/components/ui/EmptyState';
import { ERROR_SURFACE } from '@/components/ui/errorSurface';
import { Loaded } from '@/components/ui/Loaded';
import { PageHeader, Section } from '@/components/ui/PageHeader';
import { Tabs } from '@/components/ui/Tabs';
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
const RANGES = [7, 14, 30, 90].map((days) => ({ value: days, label: `${days}일` }));

/**
 * 합계 카드 옆에 붙일 일자별 계열.
 *
 * <p>합계 숫자 하나로는 <b>구간 내내 고르게 났는지, 하루에 몰렸는지</b>를 구분할 수 없다.
 * 30일 방 생성 300건은 하루 10건씩일 수도, 이벤트 하루에 250건이 몰린 것일 수도 있다.
 * 두 경우에 할 일이 완전히 다르다.
 *
 * <p>완주율과 방당 참여자는 서버가 <b>구간 합계로만</b> 준다. 여기서 일자별로 다시 만드는
 * 것은 같은 정의를 두 곳에 두는 일이라 위험한데, 나눗셈이 같으므로(완주÷생성) 값이 갈리지
 * 않는다. 대신 <b>분모가 0인 날은 건너뛰지 않고 0으로 둔다</b>. 그 날을 빼면 점 개수가
 * 줄어 다른 계열과 가로축이 어긋난다.
 */
function useDailySeries(trend: DailyTrend[] | undefined) {
  return useMemo(() => {
    const series = trend ?? [];
    return {
      created: series.map((day) => day.created),
      completionRate: series.map((day) => (day.created === 0 ? 0 : day.completed / day.created)),
      playersPerRoom: series.map((day) => (day.created === 0 ? 0 : day.players / day.created)),
    };
  }, [trend]);
}

export function AnalyticsPage() {
  const [days, setDays] = useState<number>(30);

  const period = usePeriodSummary(days);
  const trend = useTrend(Math.min(days, 90));
  const games = useGamePlayStats(days);

  const daily = useDailySeries(trend.data);

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title="서비스 분석"
        description="사람들이 이 서비스를 실제로 어떻게 쓰는지. 인프라 지표는 Grafana(status.zzol.site)가 봅니다."
        actions={<Tabs tabs={RANGES} value={days} onChange={setDays} label="조회 기간" />}
      />

      <Section
        title="기간 합계"
        description={
          period.data ? `${period.data.from} ~ ${period.data.to} (${period.data.days}일)` : undefined
        }
      >
        <Loaded query={period} skeleton={
            <TileGrid>
              <TileSkeletons />
            </TileGrid>
          } errorClassName={ERROR_SURFACE}>
          {(data) => (
            <TileGrid>
              <Tile label="방 생성" value={data.funnel.created} trend={daily.created} />
              <Tile
                label="완주율"
                value={formatPercent(data.funnel.completionRate)}
                hint={`생성된 방 중 DONE 까지 간 비율. 완주 ${data.funnel.completed}건`}
                trend={daily.completionRate}
              />
              <Tile
                label="방당 평균 참여자"
                value={data.avgPlayersPerRoom.toFixed(1)}
                suffix="명"
                hint="혼자 만들고 아무도 안 온 방도 분모에 포함"
                trend={daily.playersPerRoom}
              />
              {/* 가입은 일자별 계열이 없다. 스파크라인 자리를 다른 계열로 채우지 않는다. */}
              <Tile label="신규 가입" value={data.signups} hint="비회원도 게임은 가능합니다" />
            </TileGrid>
          )}
        </Loaded>
      </Section>

      <Card>
        <CardHeader
          title="일자별 추이"
          description="합계만 보면 어느 날 무슨 일이 있었는지가 사라집니다."
          actions={<TrendLegend />}
        />
        <CardBody>
          <Loaded query={trend} skeleton={<Skeleton className="h-[260px]" />}>
            {(data) => (
              <Suspense fallback={<Skeleton className="h-[260px]" />}>
                <TrendChart data={data} height={260} />
              </Suspense>
            )}
          </Loaded>
        </CardBody>
      </Card>

      {/* 홈과 같은 분할이다(넓은 쪽 2, 좁은 쪽 1). 퍼널은 단계 이름과 막대와 전환율이
        * 한 줄에 들어가야 해서 넓은 쪽이 맞고, 게임별 목록은 이름과 숫자뿐이라 좁아도
        * 읽힌다. 화면마다 비율을 따로 정하면 메뉴를 옮길 때 카드 모서리가 움직인다. */}
      <div className="grid gap-4 xl:grid-cols-[minmax(0,2fr)_minmax(18rem,1fr)]">
        <Card>
          <CardHeader
            title="방 진행 퍼널"
            description="구간 전체를 한 번에 셉니다. 일자별 합이 아니라서 어제 생기고 오늘 끝난 방도 이어집니다."
          />
          <CardBody>
            <Loaded
              query={period}
              skeleton={
                <div className="flex flex-col gap-2">
                  {Array.from({ length: 5 }).map((_, index) => (
                    <Skeleton key={index} className="h-7" />
                  ))}
                </div>
              }
            >
              {(data) => (
                <FunnelBar
                  stages={[
                    { label: '방 생성', count: data.funnel.created },
                    { label: '게임 시작', count: data.funnel.gameStarted },
                    { label: '미니게임 완료', count: data.funnel.miniGamePlayed },
                    { label: '룰렛 도달', count: data.funnel.rouletteReached },
                    { label: '완주', count: data.funnel.completed },
                  ]}
                />
              )}
            </Loaded>
          </CardBody>
        </Card>

        <Card>
          <CardHeader
            title="게임별 플레이"
            description="완료 기준. 비중이 0에 가까운 게임은 목록에서 뺄지 고민할 때입니다."
          />
          <Loaded
            query={games}
            skeleton={
              <div className="flex flex-col gap-2.5 px-5 pb-5">
                {Array.from({ length: 6 }).map((_, index) => (
                  <Skeleton key={index} className="h-7" />
                ))}
              </div>
            }
          >
            {(data) => <GameShareList stats={data} />}
          </Loaded>
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

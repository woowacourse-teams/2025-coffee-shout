import { MessageSquareWarning, ServerCog, ShieldBan, SpellCheck } from 'lucide-react';
import { Link } from 'react-router-dom';
import {
  useActionQueue,
  useAuditLogs,
  useDailySummary,
  useNicknameAuditQuality,
  useReportSla,
  useTrend,
} from '@/api/queries';
import { lazy, Suspense } from 'react';
import type { DailyTrend } from '@/api/types';
import { ActivityFeed } from '@/components/ActivityFeed';
import { FunnelBar } from '@/components/FunnelBar';
import { QueueCard } from '@/components/QueueCard';
import { Sparkline } from '@/components/Sparkline';
import { TrendLegend } from '@/components/TrendLegend';
import { Button } from '@/components/ui/Button';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { ErrorState, Skeleton } from '@/components/ui/EmptyState';
import { Loaded } from '@/components/ui/Loaded';
import { Tile } from '@/components/ui/Tile';
import { TileGrid, TileSkeletons } from '@/components/ui/TileGrid';
import { ERROR_SURFACE } from '@/components/ui/errorSurface';
import { PageHeader, Section } from '@/components/ui/PageHeader';
import { formatDurationMinutes, formatNumber, formatPercent } from '@/lib/format';

/**
 * recharts 는 이 차트 하나에만 쓰이는데 gzip 108KB 를 더한다. 지연 로드하면 로그인 화면과
 * 목록 화면이 그 무게를 지지 않는다. 홈은 차트가 조금 늦게 떠도 나머지가 먼저 보인다.
 *
 * <p>범례를 {@link TrendLegend} 로 떼어낸 것이 이 분리의 전제다. 같은 모듈에서 범례를
 * 정적으로 가져오면 recharts 가 메인 청크로 따라 들어와 지연 로드가 무효가 된다.
 *
 * <p>{@code Sparkline} 은 반대로 정적으로 가져온다. recharts 를 쓰지 않고 {@code polyline}
 * 하나로 그리기 때문에 무게가 없다.
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
 *
 * <h2>배치는 위 세 질문의 순서를 따른다</h2>
 *
 * <p>① 처리 대기와 운영 품질을 위에 붙여 둔다. "밀렸나"와 "잘 처리하고 있나"는 같은
 * 질문의 앞뒤인데, 운영 품질이 화면 맨 아래에 있어서 둘을 함께 보려면 스크롤해야 했다.
 *
 * <p>② 그다음이 서비스 흐름이다. 14일 추이와 오늘 숫자를 나란히 두고, 그 아래 오늘 퍼널을
 * 놓는다. 셋 다 "지금 정상인가"에 답한다.
 *
 * <p>③ 최근 조치가 맨 아래에 가로로 눕는다. 시간 순 기록이라 세로로 길고, 옆에 무엇을
 * 두든 높이가 안 맞았다.
 *
 * <p><b>게임별 플레이는 뺐다.</b> 30일 집계라 이 화면의 "지금"과 시간축이 다르고,
 * 서비스 분석 화면에 똑같은 카드가 이미 있다. 같은 카드를 두 곳에 두면 한쪽만 고치는
 * 날이 온다.
 */
export function HomePage() {
  const queue = useActionQueue();
  const summary = useDailySummary();
  const trend = useTrend(14);
  const sla = useReportSla(30);
  const auditQuality = useNicknameAuditQuality(30);
  const logs = useAuditLogs(8);

  const series = trend.data ?? [];

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title="홈"
        description="처리할 일과 서비스 흐름. 인프라 지표는 Grafana(status.zzol.site)가 봅니다."
      />

      {/* 다섯 칸이다. 격리 메시지를 맨 앞에 둔다. 나머지 넷은 사람이 판단해 줄 일이고
        * 이건 시스템이 멈춘 것이라, 0이 아닌 순간 다른 무엇보다 먼저 봐야 한다. */}
      <Section title="처리 대기" description="숫자를 누르면 해당 화면으로 갑니다.">
        {queue.isError ? (
          <Card>
            <ErrorState message={(queue.error as Error).message} onRetry={() => queue.refetch()} />
          </Card>
        ) : (
          <TileGrid columns={5}>
            {queue.isPending
              ? <TileSkeletons count={5} />
              : queue.data && (
                  <>
                    <QueueCard
                      label="격리 메시지"
                      count={queue.data.deadLetters}
                      to="/ops"
                      icon={ServerCog}
                    />
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
          </TileGrid>
        )}
      </Section>

      {/* 처리 대기 바로 아래다. 밀린 양과 처리 품질은 한 덩어리로 봐야 판단이 선다.
        * 신고 SLA 와 검열 품질은 다른 요청이라 따로 감싼다. 한 덩어리로 묶으면 한쪽이
        * 실패할 때 멀쩡한 나머지 숫자까지 사라진다. */}
      <Section
        title="운영 품질"
        description="최근 30일. 일이 밀렸는지와 잘 처리하고 있는지는 다른 질문입니다."
      >
        <TileGrid>
          <Loaded query={sla} skeleton={<TileSkeletons count={2} />} errorClassName={`col-span-2 ${ERROR_SURFACE}`}>
            {(data) => (
              <>
                <Tile
                  label="가장 오래 기다린 신고"
                  value={formatDurationMinutes(data.oldestPendingMinutes)}
                  hint="접수 후 경과"
                />
                <Tile
                  label="신고 처리 중앙값"
                  value={formatDurationMinutes(data.p50Minutes)}
                  hint={`30일 ${data.resolvedCount}건`}
                />
              </>
            )}
          </Loaded>
          <Loaded
            query={auditQuality}
            skeleton={<TileSkeletons count={2} />}
            errorClassName={`col-span-2 ${ERROR_SURFACE}`}
          >
            {(data) => (
              <>
                <Tile
                  label="검열 AI 판정 뒤집힘"
                  value={formatPercent(data.overrideRate)}
                  hint="높아지면 모델을 손볼 때"
                />
                <Tile
                  label="검열 오탐 / 미탐"
                  value={`${data.falsePositive} / ${data.falseNegative}`}
                  hint="AI가 잘못 걸렀다 / 놓쳤다"
                />
              </>
            )}
          </Loaded>
        </TileGrid>
      </Section>

      <Section title="서비스 흐름" description="오늘 숫자만으로는 0이 정상인지 알 수 없습니다.">
        {/* 왼쪽은 "어떻게 돌고 있나", 오른쪽은 "오늘 얼마나 됐나".
          * 추이는 가로가 길어야 모양이 보이고, 오늘 숫자는 세로로 쌓아야 자릿수가 비교된다. */}
        <div className="grid gap-4 xl:grid-cols-[minmax(0,2fr)_minmax(18rem,1fr)]">
          <Card>
            <CardHeader title="최근 14일" actions={<TrendLegend />} />
            <CardBody>
              <Loaded query={trend} skeleton={<Skeleton className="h-[220px]" />}>
                {(data) => (
                  <Suspense fallback={<Skeleton className="h-[220px]" />}>
                    <TrendChart data={data} />
                  </Suspense>
                )}
              </Loaded>
            </CardBody>
          </Card>

          <Card>
            <CardHeader title="오늘" description={summary.data?.date} />
            <Loaded query={summary} skeleton={<RowSkeleton rows={4} />}>
              {(data) => (
                <dl className="flex flex-col">
                  <MetricRow
                    label="방 생성"
                    value={data.funnel.created}
                    series={pick(series, 'created')}
                  />
                  <MetricRow
                    label="완주"
                    value={data.funnel.completed}
                    hint={`생성 대비 ${formatPercent(data.funnel.completionRate, 0)}`}
                    series={pick(series, 'completed')}
                  />
                  <MetricRow
                    label="참여자"
                    value={data.players}
                    hint="여러 방 참여 시 중복"
                    series={pick(series, 'players')}
                  />
                  {/* 신규 가입은 일자별 계열이 없다. 스파크라인 자리는 비워 둔다.
                    * 다른 계열의 모양을 빌려 오면 그건 이 지표의 흐름이 아니다. */}
                  <MetricRow label="신규 가입" value={data.signups} hint="비회원도 게임은 가능" />
                </dl>
              )}
            </Loaded>
          </Card>
        </div>
      </Section>

      {/* items-start 다. 격자 칸은 기본으로 늘어나는데, 그러면 짧은 퍼널 카드가 옆의
        * 조치 목록 높이까지 늘어나 아래쪽 절반이 빈 흰 판이 된다. 카드는 자기 내용만큼만
        * 차지하게 두고 아래가 어긋나는 것은 그대로 둔다. */}
      <div className="grid items-start gap-4 xl:grid-cols-[minmax(0,3fr)_minmax(20rem,2fr)]">
        <Card>
          <CardHeader
            title="오늘 방 진행 퍼널"
            description="게임 시작과 미니게임 완료의 차이가 하다가 나간 방입니다."
          />
          <CardBody>
            <Loaded query={summary} skeleton={<RowSkeleton rows={5} height="h-7" />}>
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
            title="최근 조치"
            description="Grafana 로는 볼 수 없는 기록입니다."
            actions={
              <Button asChild size="sm" variant="ghost">
                <Link to="/audit-logs">전체 보기</Link>
              </Button>
            }
          />
          <Loaded query={logs} skeleton={<RowSkeleton rows={6} />}>
            {(data) => <ActivityFeed logs={data.content} />}
          </Loaded>
        </Card>
      </div>
    </div>
  );
}

/** 추이 응답에서 계열 하나만 뽑는다. 스파크라인은 값 배열만 받는다. */
function pick(series: DailyTrend[], key: 'created' | 'completed' | 'players') {
  return series.map((day) => day[key]);
}

/** 목록형 카드의 로딩 자리. 카드마다 Array.from 을 반복해 적던 것을 모았다. */
function RowSkeleton({ rows, height = 'h-8' }: { rows: number; height?: string }) {
  return (
    <div className="flex flex-col gap-3 px-5 pb-5">
      {Array.from({ length: rows }).map((_, index) => (
        <Skeleton key={index} className={height} />
      ))}
    </div>
  );
}

/**
 * 라벨은 왼쪽, 값은 오른쪽. 값을 한 줄에 세로로 맞춰 두면 위아래로 훑을 때
 * 자릿수가 눈으로 비교된다. 카드를 나란히 두면 그 비교가 안 된다.
 *
 * <p>값과 라벨 사이에 최근 14일 흐름을 그린다. 옆 카드의 큰 차트와 같은 데이터인데,
 * 큰 차트는 세 계열이 겹쳐 있어 <b>한 지표만 따로</b> 보기가 어렵다. 여기서는 "오늘
 * 12건"이 평소만큼인지가 한 줄 안에서 끝난다.
 *
 * <p>줄 사이 구분선을 지웠다. 네 줄이 각각 하나의 위젯처럼 보여야 하는데, 가로선이
 * 그것을 표의 행으로 되돌려 놓았다.
 */
function MetricRow({
  label,
  value,
  hint,
  series,
}: {
  label: string;
  value: number | string;
  hint?: string;
  series?: number[];
}) {
  return (
    <div className="flex items-center justify-between gap-3 px-5 py-2.5">
      <dt className="min-w-0">
        <span className="block text-sm text-ink-secondary">{label}</span>
        {hint && <span className="block text-2xs text-ink-muted">{hint}</span>}
      </dt>
      <dd className="flex shrink-0 items-center gap-3">
        <Sparkline values={series ?? []} width={56} height={20} />
        {/* 숫자 칸에 폭을 준다. 폭이 없으면 자릿수에 따라 숫자와 스파크라인이 좌우로
          * 밀려서, 네 줄의 숫자가 제각각인 자리에 선다. 위아래로 훑을 때 자릿수를
          * 비교하려고 세로로 쌓은 것인데 그 비교가 안 된다. */}
        <span className="w-10 text-right text-xl font-bold leading-none tracking-metric text-ink">
          {typeof value === 'number' ? formatNumber(value) : value}
        </span>
      </dd>
    </div>
  );
}

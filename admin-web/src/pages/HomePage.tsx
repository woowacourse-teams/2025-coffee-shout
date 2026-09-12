import type { ColumnDef } from '@tanstack/react-table';
import { Hourglass, MessageSquareWarning, ServerCog, ShieldBan, SpellCheck } from 'lucide-react';
import { lazy, Suspense, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  useActionQueue,
  useDailySummary,
  useGamePlayStats,
  useInbox,
  useNicknameAuditQuality,
  usePeriodSummary,
  useProviderStats,
  useReportSla,
  useTrend,
} from '@/api/queries';
import type { DailyTrend, InboxItem, InboxKind } from '@/api/types';
import { ActionQueueStrip } from '@/components/ActionQueueStrip';
import { FunnelBar } from '@/components/FunnelBar';
import { GameShareList } from '@/components/GameShareList';
import { TrendLegend } from '@/components/TrendLegend';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { Skeleton } from '@/components/ui/EmptyState';
import { ERROR_SURFACE } from '@/components/ui/errorSurface';
import { Loaded } from '@/components/ui/Loaded';
import { PageHeader, Section } from '@/components/ui/PageHeader';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Tabs } from '@/components/ui/Tabs';
import { Tile } from '@/components/ui/Tile';
import { TileGrid, TileSkeletons } from '@/components/ui/TileGrid';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';
import { formatDurationMinutes, formatNumber, formatPercent } from '@/lib/format';
import { inboxKindLabel, reportCategoryLabel } from '@/lib/labels';

/**
 * recharts 는 이 차트 하나에만 쓰이는데 gzip 108KB 를 더한다. 지연 로드하면 로그인 화면과
 * 목록 화면이 그 무게를 지지 않는다.
 *
 * <p>범례를 {@link TrendLegend} 로 떼어낸 것이 이 분리의 전제다. 같은 모듈에서 범례를
 * 정적으로 가져오면 recharts 가 메인 청크로 따라 들어와 지연 로드가 무효가 된다.
 */
const TrendChart = lazy(() =>
  import('@/components/TrendChart').then((module) => ({ default: module.TrendChart })),
);

/** 도넛도 recharts 다. 같은 이유로 떼어 받는다. */
const ProviderDonut = lazy(() =>
  import('@/components/ProviderDonut').then((module) => ({ default: module.ProviderDonut })),
);

/** 이 화면의 유일한 좌우 분할이다. 넓은 쪽 2, 좁은 쪽 1. */
const SPLIT = 'grid items-start gap-4 xl:grid-cols-[minmax(0,2fr)_minmax(18rem,1fr)]';

/** 품질 지표의 기간. 홈은 "요즘 어떤가"를 한 값으로 본다. 기간을 바꿔 보는 것은 분석이 한다. */
const QUALITY_DAYS = 30;

/**
 * 규모와 구성을 재는 기간 선택지.
 *
 * <p>누적을 쓰지 않는다. 서비스가 지금 어느 정도로 쓰이는지를 보는 자리인데 누적은 시간이
 * 갈수록 커지기만 해서 <b>어제와 오늘의 차이를 못 말한다.</b> 반년 전에 반짝했다가 지금
 * 아무도 안 쓰는 서비스도 누적은 크다.
 *
 * <p>날짜 범위 피커를 두지 않았다. 실제로 묻는 것은 "이번 주 어때", "지난달 대비 어때"
 * 수준이고, 임의 구간이 필요한 분석은 어차피 SQL 로 간다. 피커를 두면 잘못 고른 구간으로
 * 퍼널이 이상해 보이는 일이 생긴다.
 *
 * <p>90일이 상한이다. 서버는 365까지 받지만 화면에서 열어 두지 않는다. 이 집계는 인덱스를
 * 타도 구간이 길수록 스캔이 늘고, 백오피스 한 번 열자고 운영 DB 를 길게 잡을 이유가 없다.
 */
const RANGES = [7, 14, 30, 90].map((days) => ({ value: days, label: `${days}일` }));

/**
 * 작업함에 보여 줄 줄 수.
 *
 * <p>서버는 스무 건을 주는데 여기서는 여덟 줄만 깐다. 홈은 요약이라 목록이 화면의 절반을
 * 차지하면 정작 요약이 아래로 밀린다. 종류별 건수는 위 칩이 스무 건 전부를 세고, 전체를
 * 훑을 일이면 각 화면으로 가면 된다.
 */
const PREVIEW_ROWS = 8;

type KindFilter = InboxKind | 'ALL';

/**
 * 홈.
 *
 * <p>세 구획으로 나눈다. <b>손이 필요한 것, 오늘 얼마나 돌았나, 우리가 잘 처리하고 있나.</b>
 * 구획마다 지표 칸이 먼저 서고 그 아래에 표나 그래프가 붙는다. 칸은 "얼마인가"에 답하고
 * 아래 것은 "그래서 무엇을 볼까"에 답한다.
 *
 * <p>예전 홈은 다섯 덩어리가 같은 무게로 쌓여 있었고 각 덩어리 안이 헐거웠다. 지표 두
 * 줄짜리 카드가 세 줄짜리 카드와 높이를 맞추느라 아래가 비었고 퍼널 옆 카드도 그랬다.
 * 칸을 촘촘히 깔고 남는 높이를 만들지 않는 편이 훑기 쉽다.
 *
 * <p>담는 지표의 기준은 그대로다. <b>Grafana 가 못 보는 것.</b> 응답시간, 에러율, JVM 은
 * 그쪽이 이미 본다. 여기 있는 것은 전부 도메인 조인이거나 우리 DB 에만 있는 기록이다.
 */
export function HomePage() {
  // 기간은 훅 호출보다 먼저 정해져야 한다. 규모와 구성 조회가 이 값을 받는다.
  const [scaleDays, setScaleDays] = useState(30);

  const queue = useActionQueue();
  const inbox = useInbox();
  const summary = useDailySummary();
  const trend = useTrend(14);
  const period = usePeriodSummary(scaleDays);
  const providers = useProviderStats();
  const games = useGamePlayStats(scaleDays);
  const sla = useReportSla(QUALITY_DAYS);
  const auditQuality = useNicknameAuditQuality(QUALITY_DAYS);

  const [kind, setKind] = useState<KindFilter>('ALL');

  const series = trend.data ?? [];
  const items = inbox.data ?? [];
  const filtered = kind === 'ALL' ? items : items.filter((item) => item.kind === kind);

  return (
    <div className="flex flex-col gap-7">
      <PageHeader
        title="홈"
        description="쫄이 지금 어떻게 쓰이고 있는지. 인프라 지표는 Grafana(status.zzol.site)가 봅니다."
      />

      {/* 규모가 맨 위다. 홈은 처음 열리는 화면이라 "이 서비스가 지금 어느 정도인가"에
        * 먼저 답해야 한다. 처리할 일은 그다음이다 - 손댈 게 있는지는 레일 배지가 늘
        * 말하고 있으므로 화면 맨 위를 차지할 이유가 없다. */}
      <Section
        title="서비스 규모"
        description={
          period.data ? `${period.data.from} 부터 ${period.data.to} 까지` : `최근 ${scaleDays}일`
        }
        actions={<Tabs tabs={RANGES} value={scaleDays} onChange={setScaleDays} label="조회 기간" />}
      >
        <div className="flex flex-col gap-4">
          <Loaded
            query={period}
            errorClassName={ERROR_SURFACE}
            skeleton={
              <TileGrid>
                <TileSkeletons />
              </TileGrid>
            }
          >
            {(data) => (
              <TileGrid>
                <Tile label="방 생성" value={data.funnel.created} hint={`${scaleDays}일 동안`} />
                <Tile
                  label="완주율"
                  value={formatPercent(data.funnel.completionRate)}
                  hint={`끝까지 간 방 ${formatNumber(data.funnel.completed)}건`}
                />
                <Tile
                  label="참여자"
                  value={data.players}
                  hint="사람 수가 아니라 참여 건수"
                />
                <Tile
                  label="방당 평균 참여자"
                  value={data.avgPlayersPerRoom.toFixed(1)}
                  suffix="명"
                  hint="혼자 만들고 아무도 안 온 방도 분모에 포함"
                />
              </TileGrid>
            )}
          </Loaded>

          <div className={SPLIT}>
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

            <Card>
              <CardHeader title="소셜 제공자" description="연결 수의 합은 회원 수와 다릅니다." />
              <CardBody>
                <Loaded query={providers} skeleton={<Skeleton className="h-[200px]" />}>
                  {(data) => (
                    <Suspense fallback={<Skeleton className="h-[200px]" />}>
                      <ProviderDonut stats={data} />
                    </Suspense>
                  )}
                </Loaded>
              </CardBody>
            </Card>
          </div>
        </div>
      </Section>

      <Section title="오늘" description={summary.data?.date}>
        <div className="flex flex-col gap-4">
          <Loaded
            query={summary}
            errorClassName={ERROR_SURFACE}
            skeleton={
              <TileGrid>
                <TileSkeletons />
              </TileGrid>
            }
          >
            {(data) => (
              <TileGrid>
                {/* 스파크라인이 옆에 붙는다. 오늘 숫자 하나로는 그 값이 평소보다 높은지
                  * 낮은지를 알 수 없는데, 홈에서 실제로 묻는 것은 그것이다. */}
                <Tile
                  label="방 생성"
                  value={data.funnel.created}
                  hint="오늘 만들어진 방"
                  trend={pick(series, 'created')}
                />
                <Tile
                  label="완주"
                  value={data.funnel.completed}
                  hint={`생성 대비 ${formatPercent(data.funnel.completionRate, 0)}`}
                  trend={pick(series, 'completed')}
                />
                <Tile
                  label="참여자"
                  value={data.players}
                  hint="여러 방 참여 시 중복"
                  trend={pick(series, 'players')}
                />
                <Tile label="신규 가입" value={data.signups} hint="비회원도 게임은 가능" />
              </TileGrid>
            )}
          </Loaded>

          <div className={SPLIT}>
            <Card>
              <CardHeader
                title="최근 14일"
                description="오늘 숫자만으로는 0이 정상인지 알 수 없습니다."
                actions={<TrendLegend />}
              />
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
          </div>
        </div>
      </Section>

      <Section title="처리 대기" description="숫자를 누르면 해당 화면으로 갑니다.">
        <div className="flex flex-col gap-4">
          <Loaded
            query={queue}
            errorClassName={ERROR_SURFACE}
            skeleton={
              <TileGrid columns={5}>
                <TileSkeletons count={5} height="h-[4.75rem]" />
              </TileGrid>
            }
          >
            {(data) => (
              <ActionQueueStrip
                items={[
                  {
                    label: '격리 메시지',
                    count: data.deadLetters,
                    to: '/ops',
                    icon: ServerCog,
                    critical: true,
                  },
                  {
                    label: '미처리 신고',
                    count: data.pendingReports,
                    to: '/reports',
                    icon: MessageSquareWarning,
                  },
                  {
                    label: 'AI가 걸러낸 닉네임',
                    count: data.flaggedNicknames,
                    to: '/profanity',
                    icon: SpellCheck,
                  },
                  {
                    label: 'AI가 판단 못한 닉네임',
                    count: data.pendingNicknames,
                    to: '/profanity',
                    icon: Hourglass,
                  },
                  { label: '차단 IP', count: data.blockedIps, to: '/ip-blocks', icon: ShieldBan },
                ]}
              />
            )}
          </Loaded>

          <InboxCard
            items={items}
            filtered={filtered}
            kind={kind}
            onKindChange={setKind}
            loading={inbox.isPending}
            error={inbox.error}
            onRetry={() => inbox.refetch()}
          />
        </div>
      </Section>

      <Section
        title="운영 품질"
        description={`최근 ${QUALITY_DAYS}일. 일이 밀렸는지와 잘 처리하고 있는지는 다른 질문입니다.`}
      >
        {/* 세 칸씩 두 줄로 깐다. 윗줄이 신고, 아랫줄이 검열이다. 여섯을 한 줄에 늘어놓으면
          * 서로 무관한 여섯 지표로 읽히는데 실제로는 질문이 둘이다.
          *
          * 두 요청은 따로 감싼다. 한 덩어리로 묶으면 한쪽이 실패할 때 멀쩡한 나머지
          * 숫자까지 사라진다. */}
        <div className="flex flex-col gap-3">
          <Loaded
            query={sla}
            errorClassName={ERROR_SURFACE}
            skeleton={
              <TileGrid columns={3}>
                <TileSkeletons count={3} />
              </TileGrid>
            }
          >
            {(data) => (
              <TileGrid columns={3}>
                <Tile
                  label="가장 오래 기다린 신고"
                  value={formatDurationMinutes(data.oldestPendingMinutes)}
                  hint={`미처리 ${data.pendingCount}건 중 접수가 가장 이른 건`}
                />
                <Tile
                  label="신고 처리 중앙값"
                  value={formatDurationMinutes(data.p50Minutes)}
                  hint={`${QUALITY_DAYS}일 동안 처리한 ${data.resolvedCount}건 기준`}
                />
                <Tile
                  label="신고 처리 p95"
                  value={formatDurationMinutes(data.p95Minutes)}
                  hint="스무 건 중 한 건은 이보다 오래 걸립니다"
                />
              </TileGrid>
            )}
          </Loaded>

          <Loaded
            query={auditQuality}
            errorClassName={ERROR_SURFACE}
            skeleton={
              <TileGrid columns={3}>
                <TileSkeletons count={3} />
              </TileGrid>
            }
          >
            {(data) => (
              <TileGrid columns={3}>
                <Tile
                  label="검열 판정 뒤집힘"
                  value={formatPercent(data.overrideRate)}
                  hint={`판정한 ${data.total}건 중. 높아지면 모델을 손볼 때`}
                />
                <Tile
                  label="검열 오탐"
                  value={data.falsePositive}
                  hint="AI가 걸렀는데 운영자가 허용"
                />
                <Tile
                  label="검열 미탐"
                  value={data.falseNegative}
                  hint="AI가 놓쳤는데 운영자가 차단"
                />
              </TileGrid>
            )}
          </Loaded>
        </div>
      </Section>

      {/* 이 화면이 무엇을 못 보는지 적어 둔다. 지표를 믿으려면 경계를 알아야 하고,
        * 반년 뒤에 이 숫자를 보는 사람은 여기 적힌 것을 다시 알아낼 방법이 없다.
        *
        * 서비스 분석 화면에 있던 것을 그대로 옮겨 왔다. 그 화면이 사라져도 이 설명은
        * 남아야 한다. 설명이 없으면 퍼널에 SCORE_BOARD 가 왜 없는지를 매번 다시 묻는다. */}
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
          <p>
            <b className="text-ink">소셜 제공자</b>의 연결 수 합은 회원 수와 다릅니다. 한 사람이
            구글과 카카오를 모두 연결할 수 있습니다.
          </p>
        </CardBody>
      </Card>
    </div>
  );
}

/**
 * 통합 작업함.
 *
 * <p>위 칸들이 "어디에 몇 건"을 말한다면 여기는 <b>그게 무엇인지</b>를 말한다. 칸만 있으면
 * 숫자를 보고 화면을 옮겨야 무슨 일인지 알 수 있고, 세 종류면 세 화면을 왕복해야 한다.
 */
function InboxCard({
  items,
  filtered,
  kind,
  onKindChange,
  loading,
  error,
  onRetry,
}: {
  items: InboxItem[];
  filtered: InboxItem[];
  kind: KindFilter;
  onKindChange: (kind: KindFilter) => void;
  loading: boolean;
  error: unknown;
  onRetry: () => void;
}) {
  const navigate = useNavigate();

  const columns = useMemo<ColumnDef<InboxItem, unknown>[]>(
    () => [
      {
        accessorKey: 'kind',
        header: '종류',
        meta: { width: '6rem' },
        cell: (c) => <KindBadge kind={c.getValue() as InboxKind} />,
      },
      {
        accessorKey: 'title',
        header: '내용',
        cell: (c) => <span className="line-clamp-2">{String(c.getValue())}</span>,
      },
      {
        accessorKey: 'detail',
        header: '사유',
        // 주인공은 내용 열이다. 사유에는 대개 "버그" 같은 한 단어가 들어가는데
        // 곁들이는 열이 넓으면 본문이 밀려 두 줄로 감긴다.
        meta: { width: '11rem' },
        cell: (c) => {
          const item = c.row.original;
          const detail = c.getValue() as string | null;
          if (!detail) {
            return <span className="text-ink-muted">-</span>;
          }
          const text = item.kind === 'REPORT' ? reportCategoryLabel(detail) : detail;
          return (
            <span className="line-clamp-2 text-ink-secondary" title={text}>
              {text}
            </span>
          );
        },
      },
      {
        accessorKey: 'occurredAt',
        header: '들어온 시각',
        meta: { width: '13rem' },
        cell: (c) => <Timestamp value={c.getValue() as string} />,
      },
    ],
    [],
  );

  return (
    <Card>
      <CardHeader
        title="처리할 일"
        description={`세 곳에서 모은 최신 ${items.length}건 중 앞 ${Math.min(filtered.length, PREVIEW_ROWS)}건입니다. 전체 목록은 각 화면에 있습니다.`}
        actions={
          <Tabs
            label="종류"
            value={kind}
            onChange={onKindChange}
            tabs={[
              { value: 'ALL', label: `전체 ${items.length}` },
              { value: 'REPORT', label: `신고 ${count(items, 'REPORT')}` },
              { value: 'NICKNAME', label: `닉네임 ${count(items, 'NICKNAME')}` },
              { value: 'DEAD_LETTER', label: `격리 ${count(items, 'DEAD_LETTER')}` },
            ]}
          />
        }
      />

      {/* 행을 누르면 그 종류의 화면으로 가되 해당 항목이 열린 상태로 간다.
        *
        * 여기서 바로 처리하지 않는 이유가 있다. 세 종류의 조치가 서로 다르고(신고는
        * 처리 완료, 닉네임은 허용과 차단, 격리는 재투입과 폐기), 격리는 원문을 읽지
        * 않고 누르면 안 되는 조치다. 조치 UI 세 벌을 여기 겹쳐 놓으면 작업함이 세 화면을
        * 합친 것이 아니라 <b>네 번째 화면</b>이 된다. */}
      <DataTable
        columns={columns}
        data={filtered.slice(0, PREVIEW_ROWS)}
        loading={loading}
        error={error}
        onRetry={onRetry}
        skeletonRows={8}
        onRowClick={(item) => navigate(routeOf(item))}
        emptyTitle={kind === 'ALL' ? '처리할 일이 없습니다' : '이 종류는 없습니다'}
        emptyDescription={
          kind === 'ALL'
            ? '새 신고나 검열 대기가 생기면 여기에 쌓입니다.'
            : '다른 종류를 눌러 보세요.'
        }
      />
    </Card>
  );
}

/**
 * 종류 표식.
 *
 * <p>격리만 색이 붙는다. 신고와 검열은 평소에도 쌓이는 것이 정상이라 늘 코랄이면 그 색이
 * 뜻을 잃는다. 격리는 평소 0이고 1이 되는 순간이 곧 사고다.
 */
function KindBadge({ kind }: { kind: InboxKind }) {
  return (
    <StatusBadge tone={kind === 'DEAD_LETTER' ? 'attention' : 'neutral'}>
      {inboxKindLabel(kind)}
    </StatusBadge>
  );
}

/**
 * 행을 눌렀을 때 갈 곳.
 *
 * <p>격리 메시지의 식별자는 {@code OUTBOX:3} 처럼 출처가 앞에 붙는다. 두 테이블의 id 가
 * 겹쳐서 숫자만으로는 무엇을 폐기할지 정해지지 않기 때문이다.
 */
function routeOf(item: InboxItem): string {
  if (item.kind === 'REPORT') {
    return `/reports?open=report:${item.id}`;
  }
  if (item.kind === 'NICKNAME') {
    return '/profanity';
  }
  return '/ops';
}

function count(items: InboxItem[], kind: InboxKind): number {
  return items.filter((item) => item.kind === kind).length;
}

/** 추이 응답에서 계열 하나만 뽑는다. 스파크라인은 값 배열만 받는다. */
function pick(series: DailyTrend[], key: 'created' | 'completed' | 'players') {
  return series.map((day) => day[key]);
}

/** 목록형 카드의 로딩 자리. 좌우 여백은 CardBody 가 준다. */
function RowSkeleton({ rows, height = 'h-8' }: { rows: number; height?: string }) {
  return (
    <div className="flex flex-col gap-3 py-1">
      {Array.from({ length: rows }).map((_, index) => (
        <Skeleton key={index} className={height} />
      ))}
    </div>
  );
}

import type { ColumnDef } from '@tanstack/react-table';
import {
  DoorOpen,
  Flag,
  Hourglass,
  MessageSquareWarning,
  ServerCog,
  ShieldBan,
  SpellCheck,
  UserPlus,
  Users,
} from 'lucide-react';
import { lazy, Suspense, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useActionQueue, useDailySummary, useInbox, useTrend } from '@/api/queries';
import type { DailyTrend, InboxItem, InboxKind } from '@/api/types';
import { ActionQueueStrip } from '@/components/ActionQueueStrip';
import { TrendLegend } from '@/components/TrendLegend';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { Skeleton } from '@/components/ui/EmptyState';
import { ERROR_SURFACE } from '@/components/ui/errorSurface';
import { Loaded } from '@/components/ui/Loaded';
import { PageHeader } from '@/components/ui/PageHeader';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Tabs } from '@/components/ui/Tabs';
import { Tile } from '@/components/ui/Tile';
import { TileGrid, TileSkeletons } from '@/components/ui/TileGrid';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';
import { formatPercent } from '@/lib/format';
import { inboxKindLabel, reportCategoryLabel } from '@/lib/labels';

/**
 * recharts 는 이 차트 하나에만 쓰이는데 gzip 108KB 를 더한다. 지연 로드하면 로그인 화면이
 * 그 무게를 지지 않는다. 범례를 {@link TrendLegend} 로 떼어낸 것이 이 분리의 전제다.
 */
const TrendChart = lazy(() =>
  import('@/components/TrendChart').then((module) => ({ default: module.TrendChart })),
);

/** 이 화면의 유일한 좌우 분할이다. 넓은 쪽 2, 좁은 쪽 1. */
const SPLIT = 'grid items-start gap-4 xl:grid-cols-[minmax(0,2fr)_minmax(17rem,1fr)]';

/**
 * 작업함에 보여 줄 줄 수.
 *
 * <p>서버는 스무 건을 주는데 여기서는 여덟 줄만 깐다. 홈은 요약이라 목록이 화면의 절반을
 * 차지하면 정작 요약이 아래로 밀린다. 종류별 건수는 칩이 스무 건 전부를 세고, 전체를
 * 훑을 일이면 각 화면으로 가면 된다.
 */
const PREVIEW_ROWS = 8;

type KindFilter = InboxKind | 'ALL';

/**
 * 홈.
 *
 * <p>세 가지만 답한다. <b>오늘 얼마나 돌았나, 평소와 다른가, 지금 손댈 게 무엇인가.</b>
 *
 * <p>한때 여기에 여덟 덩어리가 있었다. 서비스 규모, 게임별 플레이, 소셜 제공자, 오늘,
 * 추이, 퍼널, 처리 대기, 운영 품질, 그리고 경계 설명. 전부 맞는 지표였지만 첫 화면이
 * 4700px 이 되면서 <b>무엇부터 봐야 하는지가 사라졌다.</b> 요약은 적은 수의 핵심이지
 * 모든 것을 한 장에 얹는 일이 아니다.
 *
 * <p>덜어낸 기준은 <b>매일 여는 화면에서 필요한가</b>였다. 운영 품질은 신고 화면과 검열
 * 화면이 같은 숫자를 이미 보여주므로 여기서는 잃는 게 없다. 게임별 비중과 소셜 제공자
 * 분포는 분기에 한 번 볼 구성이지 아침마다 볼 것이 아니다.
 *
 * <p>담는 지표의 기준은 그대로다. <b>Grafana 가 못 보는 것.</b> 응답시간, 에러율, JVM 은
 * 그쪽이 이미 본다.
 */
export function HomePage() {
  const queue = useActionQueue();
  const inbox = useInbox();
  const summary = useDailySummary();
  const trend = useTrend(14);

  const [kind, setKind] = useState<KindFilter>('ALL');

  const series = trend.data ?? [];
  const items = inbox.data ?? [];
  const filtered = kind === 'ALL' ? items : items.filter((item) => item.kind === kind);

  return (
    <div className="flex flex-col gap-4">
      <PageHeader
        title="홈"
        description="오늘 서비스가 얼마나 돌았고 지금 손댈 게 무엇인지. 인프라 지표는 Grafana(status.zzol.site)가 봅니다."
      />

      {/* 오늘 숫자에 스파크라인이 붙는다. 숫자 하나로는 그 값이 평소보다 높은지 낮은지를
        * 알 수 없는데, 아침에 홈을 여는 사람이 실제로 묻는 것은 그것이다. */}
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
            <Tile
              label="오늘 방 생성"
              icon={DoorOpen}
              value={data.funnel.created}
              hint="오늘 만들어진 방"
              delta={deltaOf(series, 'created')}
              trend={pick(series, 'created')}
            />
            <Tile
              label="오늘 완주"
              icon={Flag}
              value={data.funnel.completed}
              hint={`생성 대비 ${formatPercent(data.funnel.completionRate, 0)}`}
              delta={deltaOf(series, 'completed')}
              trend={pick(series, 'completed')}
            />
            <Tile
              label="오늘 참여자"
              icon={Users}
              value={data.players}
              hint="여러 방 참여 시 중복"
              delta={deltaOf(series, 'players')}
              trend={pick(series, 'players')}
            />
            {/* 가입은 일자별 계열이 없다. 스파크라인 자리를 다른 계열로 채우지 않는다.
              * 빌려온 모양은 이 지표의 흐름이 아니다. */}
            <Tile
              label="오늘 신규 가입"
              icon={UserPlus}
              value={data.signups}
              hint="비회원도 게임은 가능"
            />
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
          <CardHeader title="처리 대기" description="줄을 누르면 해당 화면으로 갑니다." />
          <Loaded
            query={queue}
            skeleton={
              <div className="flex flex-col gap-2 px-5 pb-5">
                {Array.from({ length: 5 }).map((_, index) => (
                  <Skeleton key={index} className="h-8" />
                ))}
              </div>
            }
          >
            {(data) => (
              <ActionQueueStrip
                items={[
                  // 맨 앞이다. 나머지 넷은 사람이 판단해 줄 일이고 쌓이는 게 정상이지만,
                  // 이건 시스템이 멈춘 것이라 0이 아닌 순간 다른 무엇보다 먼저 봐야 한다.
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
                  // 화면에 FLAGGED, PENDING 을 그대로 적지 않는다. 서버 enum 이름이고
                  // 운영자가 쓰는 말이 아니다.
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
        </Card>
      </div>

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
  );
}

/**
 * 통합 작업함.
 *
 * <p>옆 칸들이 "어디에 몇 건"을 말한다면 여기는 <b>그게 무엇인지</b>를 말한다. 숫자만
 * 있으면 화면을 옮겨야 무슨 일인지 알 수 있고, 세 종류면 세 화면을 왕복해야 한다.
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
        skeletonRows={PREVIEW_ROWS}
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

/**
 * 어제 대비 증감.
 *
 * <p>스파크라인이 모양을 말한다면 이 숫자는 <b>얼마나 달라졌는지</b>를 말한다. 아침에 홈을
 * 여는 사람이 묻는 것이 그것이라, 그림만으로는 "조금 줄었다"까지밖에 못 읽는다.
 *
 * <p>어제가 없으면(추이가 하루치뿐이면) 아무것도 돌려주지 않는다. 0을 돌려주면 {@code Tile}
 * 이 "변화 없음"으로 그리는데, 그건 잰 적이 없는 것과 다른 말이다.
 */
function deltaOf(series: DailyTrend[], key: 'created' | 'completed' | 'players') {
  if (series.length < 2) {
    return undefined;
  }
  const today = series[series.length - 1]?.[key] ?? 0;
  const yesterday = series[series.length - 2]?.[key] ?? 0;
  return today - yesterday;
}

import type { ColumnDef } from '@tanstack/react-table';
import { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuditLogs, useDailySummary, useInbox, useTrend } from '@/api/queries';
import type { DailyTrend, InboxItem, InboxKind } from '@/api/types';
import { ActivityFeed } from '@/components/ActivityFeed';
import { Button } from '@/components/ui/Button';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { Skeleton } from '@/components/ui/EmptyState';
import { Loaded } from '@/components/ui/Loaded';
import { MetricRow } from '@/components/ui/MetricRow';
import { PageHeader } from '@/components/ui/PageHeader';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Tabs } from '@/components/ui/Tabs';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';
import { formatPercent } from '@/lib/format';
import { inboxKindLabel, reportCategoryLabel } from '@/lib/labels';

/**
 * 이 화면의 유일한 좌우 분할이다. 넓은 쪽 2, 좁은 쪽 1.
 *
 * <p>{@code items-start} 를 준다. 기본값(stretch)이면 오른쪽 카드가 왼쪽 표 높이까지
 * 늘어나 <b>아래 절반이 빈 흰 판</b>이 된다. 표는 스무 행이고 오른쪽 목록은 네 줄이라
 * 그 차이가 크다. 카드 사이로 캔버스가 보이는 것은 흠이 아니다. 흠은 카드 <b>안</b>이
 * 비는 것이다.
 */
const SPLIT = 'grid items-start gap-4 xl:grid-cols-[minmax(0,2fr)_minmax(18rem,1fr)]';

type KindFilter = InboxKind | 'ALL';

/**
 * 작업함.
 *
 * <p>예전 홈은 대시보드였다. 처리 대기 다섯 칸, 운영 품질 두 카드, 14일 추이, 오늘 퍼널,
 * 최근 조치가 같은 무게로 쌓여 세로 2600px 였고, 숫자를 보여준 뒤에는 <b>다른 화면으로
 * 던졌다.</b> 운영자는 숫자를 보러 오는 게 아니라 일을 끝내러 온다.
 *
 * <p>그래서 홈이 답하는 질문을 하나로 좁혔다. <b>지금 무엇을 처리해야 하는가.</b> 신고와
 * 닉네임 검열과 격리 메시지가 세 화면에 흩어져 있어 오늘 할 일을 끝내려면 세 화면을
 * 왕복해야 했다. 여기서 한 목록으로 훑는다.
 *
 * <p>"평소만큼 돌고 있나"는 서비스 분석이 답한다. 추이와 퍼널과 운영 품질을 그쪽으로
 * 옮겼다. 레일에서 한 번 누르는 거리이고, 두 질문을 한 화면에 쌓았기 때문에 홈이
 * 길어졌던 것이다. 오른쪽의 오늘 숫자와 스파크라인이 "평소와 다른가"에는 답한다.
 */
export function HomePage() {
  const navigate = useNavigate();
  const inbox = useInbox();
  const summary = useDailySummary();
  const trend = useTrend(14);
  // 다섯 건이다. 더 보려면 전체 보기로 간다.
  const logs = useAuditLogs(5);

  const [kind, setKind] = useState<KindFilter>('ALL');

  const items = inbox.data ?? [];
  const filtered = kind === 'ALL' ? items : items.filter((item) => item.kind === kind);
  const series = trend.data ?? [];

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
        // 주인공은 내용 열이다. 사유가 16rem 을 차지하고 있어서 정작 신고 본문이 두 줄로
        // 감겼는데, 사유에는 대개 "버그" 같은 한 단어가 들어간다. 곁들이는 열이 본문을
        // 밀어내면 표를 훑는 속도가 그만큼 떨어진다.
        meta: { width: '11rem' },
        cell: (c) => {
          const item = c.row.original;
          const detail = c.getValue() as string | null;
          if (!detail) {
            return <span className="text-ink-muted">-</span>;
          }
          // 신고의 사유 자리에는 카테고리가 온다. 서버 enum 이라 한글로 바꾼다.
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
    <div className="flex flex-col gap-6">
      <PageHeader
        title="작업함"
        description="신고와 닉네임 검열과 격리 메시지를 한 목록으로 봅니다. 인프라 지표는 Grafana(status.zzol.site)가 봅니다."
      />

      <div className={SPLIT}>
        <Card>
          <CardHeader
            title="처리할 일"
            // 스무 건 상한을 화면에 적는다. 적지 않으면 이 목록이 전부인 줄 알고,
            // 신고 서른아홉 건이 밀려 있어도 열두 건만 있는 것으로 읽는다.
            description="세 곳에서 모은 최신 20건입니다. 전체 목록은 각 화면에 있습니다."
            actions={
              <Tabs
                label="종류"
                value={kind}
                onChange={setKind}
                tabs={[
                  { value: 'ALL', label: `전체 ${items.length}` },
                  { value: 'REPORT', label: `신고 ${count(items, 'REPORT')}` },
                  {
                    value: 'NICKNAME',
                    label: `닉네임 ${count(items, 'NICKNAME')}`,
                  },
                  {
                    value: 'DEAD_LETTER',
                    label: `격리 ${count(items, 'DEAD_LETTER')}`,
                  },
                ]}
              />
            }
          />

          {/* 행을 누르면 그 종류의 화면으로 가되 해당 항목이 열린 상태로 간다.
           *
           * 여기서 바로 처리하지 않는 이유가 있다. 세 종류의 조치가 서로 다르고
           * (신고는 처리 완료, 닉네임은 허용과 차단, 격리는 재투입과 폐기), 격리는
           * 원문을 읽지 않고 누르면 안 되는 조치다. 조치 UI 세 벌을 여기 겹쳐 놓으면
           * 작업함이 세 화면을 합친 것이 아니라 <b>네 번째 화면</b>이 된다.
           *
           * 지금은 신고만 패널로 열린다. 나머지 둘은 그 화면에 패널이 생기는 대로
           * 같은 주소 규칙으로 이어진다. */}
          <DataTable
            columns={columns}
            data={filtered}
            loading={inbox.isPending}
            error={inbox.error}
            onRetry={() => inbox.refetch()}
            onRowClick={(item) => navigate(routeOf(item))}
            emptyTitle={kind === 'ALL' ? '처리할 일이 없습니다' : '이 종류는 없습니다'}
            emptyDescription={
              kind === 'ALL'
                ? '새 신고나 검열 대기가 생기면 여기에 쌓입니다.'
                : '다른 종류를 눌러 보세요.'
            }
          />
        </Card>

        <div className="flex flex-col gap-4">
          <Card>
            <CardHeader title="오늘" description={summary.data?.date} />
            <CardBody>
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
                    {/* 신규 가입은 일자별 계열이 없다. 빈 배열을 줘서 그림 없이 자리만
                     * 잡는다. 다른 계열의 모양을 빌려 오면 그건 이 지표의 흐름이 아니고,
                     * 아예 안 주면 이 줄만 숫자가 왼쪽으로 밀린다. */}
                    <MetricRow
                      label="신규 가입"
                      value={data.signups}
                      hint="비회원도 게임은 가능"
                      series={[]}
                    />
                  </dl>
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
            <Loaded query={logs} skeleton={<RowSkeleton rows={5} />}>
              {(data) => <ActivityFeed logs={data.content} />}
            </Loaded>
          </Card>
        </div>
      </div>
    </div>
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
 * 목록형 카드의 로딩 자리. 카드마다 Array.from 을 반복해 적던 것을 모았다.
 *
 * <p>좌우 여백이 없다. 전부 {@code CardBody} 안에서 쓰이므로 여백은 거기서 온다.
 */
function RowSkeleton({ rows, height = 'h-8' }: { rows: number; height?: string }) {
  return (
    <div className="flex flex-col gap-3 py-1">
      {Array.from({ length: rows }).map((_, index) => (
        <Skeleton key={index} className={height} />
      ))}
    </div>
  );
}

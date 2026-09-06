import type { ColumnDef } from '@tanstack/react-table';
import { MessageSquareWarning, ShieldBan, SpellCheck, Zap } from 'lucide-react';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Timestamp } from '@/components/ui/Timestamp';
import { EmptyState, ErrorState, Skeleton } from '@/components/ui/EmptyState';
import { QueueCard } from '@/components/QueueCard';
import { StatCard } from '@/components/StatCard';
import { FunnelBar } from '@/components/FunnelBar';
import { DataTable } from '@/components/DataTable';
import { formatDurationMinutes, formatPercent } from '@/lib/format';

/**
 * 컴포넌트 갤러리. Storybook 을 두지 않은 대신이다.
 *
 * <p>내부 도구에 시각 회귀 테스트까지 붙이는 것은 과하다. 프리미티브를 한눈에 확인하고
 * 새로 만들 때 기존 것을 먼저 찾아보게 하는 목적이라면 이 한 페이지로 충분하다.
 */
export function DesignGalleryPage() {
  return (
    <div className="flex flex-col gap-6">
      <header>
        <h2 className="text-xl font-semibold text-ink">디자인 갤러리</h2>
        <p className="mt-1 text-xs text-ink-muted">
          새 컴포넌트를 만들기 전에 여기서 먼저 찾아본다. 같은 동작에 다른 모양을 쓰지 않는다.
        </p>
      </header>

      <Section title="색" description="원색이 아니라 역할 이름으로 쓴다. 팔레트가 바뀌면 별칭만 고친다.">
        <div className="flex flex-wrap gap-3">
          <Swatch name="action" className="bg-action" note="Primary 버튼, 레일" />
          <Swatch name="accent" className="bg-accent" note="브랜드. 텍스트 불가" />
          <Swatch name="accent-ink" className="bg-accent-ink" note="브랜드 텍스트" />
          <Swatch name="success" className="bg-success" />
          <Swatch name="warning" className="bg-warning" />
          <Swatch name="info" className="bg-info" />
          <Swatch name="danger" className="bg-danger" />
        </div>
        {/* 클래스명을 문자열로 조립하지 않는다. Tailwind 는 소스를 정적으로 훑어
         * 쓰인 클래스만 CSS 로 내보내므로, `bg-${name}` 은 스캔에 안 잡혀 스타일이 사라진다. */}
        <div className="mt-3 flex flex-wrap gap-3">
          {CHART_SWATCHES.map((swatch) => (
            <Swatch key={swatch.name} name={swatch.name} className={swatch.className} />
          ))}
        </div>
      </Section>

      <Section
        title="버튼"
        description="Primary 는 채움, danger 는 아웃라인. 채움 빨강은 확인 다이얼로그에서만."
      >
        <div className="flex flex-wrap items-center gap-2">
          <Button variant="primary">저장</Button>
          <Button variant="secondary">취소</Button>
          <Button variant="ghost">더 보기</Button>
          <Button variant="danger">차단 해제</Button>
          <Button variant="dangerSolid">영구 삭제</Button>
          <Button variant="primary" disabled>
            비활성
          </Button>
        </div>
        <div className="mt-3 flex flex-wrap items-center gap-2">
          <Button size="sm">작게</Button>
          <Button size="md">기본</Button>
          <Button size="lg">크게</Button>
        </div>
      </Section>

      <Section title="상태 배지" description="색 + 점 + 텍스트 3중 인코딩. 흑백 스크린샷에서도 읽힌다.">
        <div className="flex flex-wrap gap-2">
          <StatusBadge tone="neutral">미처리</StatusBadge>
          <StatusBadge tone="success">처리 완료</StatusBadge>
          <StatusBadge tone="warning">검토 대기</StatusBadge>
          <StatusBadge tone="info">진행 중</StatusBadge>
          <StatusBadge tone="danger">차단됨</StatusBadge>
          <StatusBadge tone="accent">신규</StatusBadge>
        </div>
      </Section>

      <Section title="처리 대기 큐" description="0이면 가라앉고, 1 이상이면 강조되고, 임계를 넘으면 위험.">
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <QueueCard label="미처리 신고" count={0} to="/reports" icon={MessageSquareWarning} />
          <QueueCard label="검열 대기" count={7} to="/profanity" icon={SpellCheck} />
          <QueueCard label="차단 IP" count={42} threshold={20} to="/ip-blocks" icon={ShieldBan} />
          <QueueCard label="DLQ 적체" count={3} threshold={1} to="/ops" icon={Zap} />
        </div>
      </Section>

      <Section title="지표 카드">
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <StatCard label="방 생성" value={128} delta={12} />
          <StatCard label="게임 완료" value={94} delta={-6} />
          <StatCard label="참여자" value={412} hint="같은 사람이 여러 방에 들어가면 중복 집계" />
          <StatCard label="완주율" value={formatPercent(0.734)} suffix="DONE 기준" />
        </div>
      </Section>

      <Section
        title="퍼널"
        description="막대 길이는 1단계 대비. 오른쪽 숫자는 앞 단계 대비 전환율과 이탈 수."
      >
        <Card>
          <CardBody>
            <FunnelBar
              stages={[
                { label: '방 생성', count: 128 },
                { label: '2인 이상 입장', count: 96 },
                { label: '게임 시작', count: 88 },
                { label: '룰렛 도달', count: 71 },
                { label: '완주', count: 68 },
              ]}
            />
          </CardBody>
        </Card>
      </Section>

      <Section title="표" description="모든 목록이 이 하나를 쓴다. 정렬, 빈 상태, 로딩이 화면마다 같다.">
        <Card>
          <CardHeader
            title="신고"
            description="예시 데이터"
            actions={<Button size="sm">필터</Button>}
          />
          <DataTable
            columns={SAMPLE_COLUMNS}
            data={SAMPLE_ROWS}
            emptyTitle="미처리 신고가 없습니다"
            onRowClick={() => {}}
          />
        </Card>
      </Section>

      <Section title="시각 표기" description="절대시각과 상대시각을 함께. 둘 다 필요하다.">
        <div className="flex flex-col gap-1">
          <Timestamp value={new Date(Date.now() - 12 * 60_000)} />
          <Timestamp value={new Date(Date.now() - 3 * 24 * 3600_000)} />
          <Timestamp value={null} />
          <p className="mt-2 text-xs text-ink-muted">
            소요 시간: {formatDurationMinutes(195)} / {formatDurationMinutes(2880)}
          </p>
        </div>
      </Section>

      <Section title="빈 상태와 오류" description="화면마다 문구를 따로 준다. 돌려쓰지 않는다.">
        <div className="grid gap-3 lg:grid-cols-3">
          <Card>
            <EmptyState
              title="미처리 신고가 없습니다"
              description="새 신고가 들어오면 여기에 쌓입니다."
            />
          </Card>
          <Card>
            <ErrorState message="서버가 응답하지 않습니다 (504)" onRetry={() => {}} />
          </Card>
          <Card>
            <CardBody className="flex flex-col gap-2">
              <Skeleton className="h-3 w-32" />
              <Skeleton className="h-3 w-full" />
              <Skeleton className="h-3 w-3/4" />
            </CardBody>
          </Card>
        </div>
      </Section>
    </div>
  );
}

function Section({
  title,
  description,
  children,
}: {
  title: string;
  description?: string;
  children: React.ReactNode;
}) {
  return (
    <section>
      <h3 className="text-sm font-semibold text-ink">{title}</h3>
      {description && <p className="mb-2 mt-0.5 text-xs text-ink-muted">{description}</p>}
      <div className={description ? '' : 'mt-2'}>{children}</div>
    </section>
  );
}

function Swatch({ name, className, note }: { name: string; className: string; note?: string }) {
  return (
    <div className="w-36">
      <div className={`h-10 rounded border border-border-default ${className}`} />
      <p className="mt-1 font-mono text-2xs text-ink-secondary">{name}</p>
      {note && <p className="text-2xs text-ink-muted">{note}</p>}
    </div>
  );
}

const CHART_SWATCHES = [
  { name: 'chart-1', className: 'bg-chart-1' },
  { name: 'chart-2', className: 'bg-chart-2' },
  { name: 'chart-3', className: 'bg-chart-3' },
  { name: 'chart-4', className: 'bg-chart-4' },
  { name: 'chart-5', className: 'bg-chart-5' },
  { name: 'chart-6', className: 'bg-chart-6' },
] as const;

type SampleRow = { id: number; category: string; content: string; status: string; createdAt: string };

const SAMPLE_ROWS: SampleRow[] = [
  {
    id: 1,
    category: 'BUG',
    content: '레이싱 게임에서 화면이 멈춥니다',
    status: 'PENDING',
    createdAt: new Date(Date.now() - 40 * 60_000).toISOString(),
  },
  {
    id: 2,
    category: 'SUGGESTION',
    content: '방 인원을 더 늘려주세요',
    status: 'RESOLVED',
    createdAt: new Date(Date.now() - 26 * 3600_000).toISOString(),
  },
];

const SAMPLE_COLUMNS: ColumnDef<SampleRow, unknown>[] = [
  { accessorKey: 'id', header: 'ID', cell: (c) => <span className="font-mono text-xs">{String(c.getValue())}</span> },
  { accessorKey: 'category', header: '카테고리' },
  { accessorKey: 'content', header: '내용' },
  {
    accessorKey: 'status',
    header: '상태',
    cell: (c) =>
      c.getValue() === 'PENDING' ? (
        <StatusBadge tone="warning">미처리</StatusBadge>
      ) : (
        <StatusBadge tone="success">처리 완료</StatusBadge>
      ),
  },
  {
    accessorKey: 'createdAt',
    header: '접수',
    cell: (c) => <Timestamp value={c.getValue() as string} />,
  },
];

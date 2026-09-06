import type { ColumnDef } from '@tanstack/react-table';
import { MessageSquareWarning, ShieldBan, SpellCheck, Zap } from 'lucide-react';
import { useState } from 'react';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { CodeBlock } from '@/components/ui/CodeBlock';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';
import { Input, Label, SearchInput, Select } from '@/components/ui/Field';
import { KeyValue } from '@/components/ui/KeyValue';
import { Pagination } from '@/components/ui/Pagination';
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
  const [confirmOpen, setConfirmOpen] = useState(false);

  return (
    <div className="flex flex-col gap-8">
      <header>
        <h2 className="text-xl font-semibold text-ink">디자인 갤러리</h2>
        <p className="mt-1 text-xs text-ink-muted">
          새 컴포넌트를 만들기 전에 여기서 먼저 찾아본다. 같은 동작에 다른 모양을 쓰지 않는다.
        </p>
      </header>

      <Section
        title="색"
        description="회색과 로고색(#FD6C6E), 둘뿐이다. 짙은 빨강 단계를 두지 않는다. 로고색은 채움과 마크로만 쓰고, 글자는 잉크가 진다."
      >
        <div className="flex flex-wrap gap-3">
          <Swatch name="action" className="bg-action" note="Primary 버튼" />
          <Swatch name="accent" className="bg-accent" note="로고색. 아이덴티티" />
          <Swatch name="attention-mark" className="bg-attention-mark" note="신호 마크" />
          <Swatch name="attention-solid" className="bg-attention-solid" note="채움 + 잉크 6.38:1" />
          <Swatch name="attention-bg" className="bg-attention-bg" note="틴트" />
          <Swatch name="attention" className="bg-attention" note="신호 옆 글자 = 잉크" />
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
        description="Primary 는 중성 채움, danger 는 코랄 테두리에 잉크 글자. 코랄 채움은 확인 다이얼로그에서만."
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

      <Section title="상태 배지" description="셋뿐이다. 손이 필요한 것만 색이 붙고 끝난 것은 물러난다.">
        <div className="flex flex-wrap gap-2">
          <StatusBadge tone="attention">미처리</StatusBadge>
          <StatusBadge tone="neutral">진행 중</StatusBadge>
          <StatusBadge tone="muted">처리 완료</StatusBadge>
        </div>
      </Section>

      <Section title="처리 대기 큐" description="0이냐 아니냐 두 단계뿐. 3건이든 42건이든 할 일은 같다.">
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <QueueCard label="미처리 신고" count={0} to="/reports" icon={MessageSquareWarning} />
          <QueueCard label="검열 대기" count={7} to="/profanity" icon={SpellCheck} />
          <QueueCard label="차단 IP" count={42} to="/ip-blocks" icon={ShieldBan} />
          <QueueCard label="DLQ 적체" count={3} to="/ops" icon={Zap} />
        </div>
      </Section>

      <Section title="지표 카드">
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <StatCard label="방 생성" value={128} delta={12} />
          <StatCard label="게임 완료" value={94} delta={-6} />
          <StatCard label="참여자" value={412} hint="여러 방 참여 시 중복 집계" />
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

      <Section title="입력" description="검색은 디바운스로 즉시 반영된다. 누를 필요 없는 버튼을 두지 않는다.">
        <div className="flex flex-wrap items-end gap-3">
          <SearchInput placeholder="닉네임 또는 유저코드" className="w-64" />
          <Select defaultValue="" className="w-40">
            <option value="">전체 상태</option>
            <option value="PENDING">미처리</option>
            <option value="RESOLVED">처리 완료</option>
          </Select>
          <Label className="w-40" text="관리자 이메일" hint="쉼표로 구분">
            <Input placeholder="admin@zzol.site" />
          </Label>
        </div>
      </Section>

      <Section title="상세 속성" description="dl 로 쓴다. 라벨과 값의 관계가 마크업에 남는다.">
        <Card>
          <CardBody>
            <KeyValue
              items={[
                { label: 'joinCode', value: <span className="font-mono">ABCDE</span> },
                { label: '상태', value: <StatusBadge tone="muted">완주</StatusBadge> },
                { label: '생성', value: <Timestamp value={new Date(Date.now() - 3600_000)} /> },
                { label: '참여자', value: '4명' },
                { label: '메모', value: null, full: true },
              ]}
            />
          </CardBody>
        </Card>
      </Section>

      <Section title="원문 뷰어" description="운영 대화는 대개 원문을 슬랙에 붙이며 시작한다.">
        <CodeBlock
          value={'{\n  "joinCode": "ABCDE",\n  "winnerProbability": 12,\n  "createdAt": "2026-09-06T14:32:00+09:00"\n}'}
          maxHeightClassName="max-h-40"
        />
      </Section>

      <Section title="확인 창" description="취소가 기본 포커스. 조치 대상을 다시 보여준다.">
        <Button variant="danger" onClick={() => setConfirmOpen(true)}>
          IP 차단 해제하기
        </Button>
        <ConfirmDialog
          open={confirmOpen}
          onOpenChange={setConfirmOpen}
          title="이 IP의 차단을 해제할까요?"
          description="해제하면 즉시 접속이 가능해집니다. 다시 차단하려면 조건이 재발생해야 합니다."
          target="1.2.3.4 · 남은 차단 시간 23시간"
          confirmLabel="차단 해제하기"
          onConfirm={() => setConfirmOpen(false)}
        />
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

      <Section title="페이지네이션" description="번호를 나열하지 않는다. 특정 건은 검색이 찾는다.">
        <Card>
          <Pagination page={2} totalPages={9} totalElements={173} onChange={() => {}} />
        </Card>
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
        <StatusBadge tone="attention">미처리</StatusBadge>
      ) : (
        <StatusBadge tone="muted">처리 완료</StatusBadge>
      ),
  },
  {
    accessorKey: 'createdAt',
    header: '접수',
    cell: (c) => <Timestamp value={c.getValue() as string} />,
  },
];

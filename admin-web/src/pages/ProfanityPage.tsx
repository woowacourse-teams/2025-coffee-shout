import type { ColumnDef } from '@tanstack/react-table';
import { useMemo, useState } from 'react';
import { useAuditDecision, useNicknameAuditQuality, useNicknameAudits } from '@/api/queries';
import type { NicknameAudit, NicknameAuditStatus } from '@/api/types';
import { Button } from '@/components/ui/Button';
import { Card, CardHeader } from '@/components/ui/Card';
import { ErrorState } from '@/components/ui/EmptyState';
import { PageHeader } from '@/components/ui/PageHeader';
import { Pagination } from '@/components/ui/Pagination';
import { StatCard } from '@/components/StatCard';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';
import { ProfanityWordsCard } from '@/components/ProfanityWordsCard';
import { cn } from '@/lib/cn';
import { formatPercent } from '@/lib/format';

const TABS: { value: NicknameAuditStatus; label: string; hint: string }[] = [
  { value: 'FLAGGED', label: 'FLAGGED', hint: 'AI가 걸러낸 닉네임' },
  { value: 'PENDING', label: 'PENDING', hint: 'AI가 판단하지 못한 닉네임' },
];

export function ProfanityPage() {
  const [status, setStatus] = useState<NicknameAuditStatus>('FLAGGED');
  const [page, setPage] = useState(0);

  const audits = useNicknameAudits(status, page);
  const quality = useNicknameAuditQuality(30);
  const decide = useAuditDecision();

  const columns = useMemo<ColumnDef<NicknameAudit, unknown>[]>(
    () => [
      {
        accessorKey: 'nickname',
        header: '닉네임',
        meta: { width: '12rem' },
        cell: (c) => <span className="font-medium">{String(c.getValue())}</span>,
      },
      {
        id: 'confidence',
        header: 'AI 신뢰도',
        meta: { width: '10rem', align: 'right' },
        accessorFn: (row) => row.confidence?.value ?? 0,
        cell: (c) => {
          const value = Number(c.getValue());
          return (
            <span className="inline-flex items-center justify-end gap-2">
              {/* 막대를 함께 그린다. 숫자만 보면 0.62와 0.91의 차이가 눈에 안 들어온다. */}
              <span className="h-1.5 w-16 overflow-hidden rounded-full bg-subtle">
                <span
                  className="block h-full rounded-full bg-accent"
                  style={{ width: `${Math.round(value * 100)}%` }}
                />
              </span>
              <span className="w-9 text-right tabular-nums">{value.toFixed(2)}</span>
            </span>
          );
        },
      },
      { accessorKey: 'reason', header: '사유' },
      {
        accessorKey: 'createdAt',
        header: '접수',
        meta: { width: '13rem' },
        cell: (c) => <Timestamp value={c.getValue() as string} />,
      },
      {
        id: 'actions',
        header: '',
        meta: { width: '10rem', align: 'right' },
        cell: (c) => (
          <span className="inline-flex gap-1.5">
            {/* 확인 창을 두지 않는다. 검열 판정은 되돌릴 수 있고(반대 버튼을 누르면 된다)
              * 한 번에 수십 건을 처리하는 화면이라 매번 창이 뜨면 일이 안 된다. */}
            <Button
              size="sm"
              disabled={decide.isPending}
              onClick={() => decide.mutate({ id: c.row.original.id, decision: 'allow' })}
            >
              허용
            </Button>
            <Button
              size="sm"
              variant="danger"
              disabled={decide.isPending}
              onClick={() => decide.mutate({ id: c.row.original.id, decision: 'block' })}
            >
              차단
            </Button>
          </span>
        ),
      },
    ],
    [decide],
  );

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title="닉네임 검열"
        description="AI가 걸러낸 닉네임을 사람이 확인합니다. 뒤집힌 비율이 모델을 손볼 시점을 알려줍니다."
      />

      <div className="grid grid-cols-2 gap-3 xl:grid-cols-4">
        <StatCard
          label="AI 판정 뒤집힘"
          value={quality.data ? formatPercent(quality.data.overrideRate) : '-'}
          hint="최근 30일. 높으면 모델 점검"
        />
        <StatCard
          label="오탐"
          value={quality.data?.falsePositive ?? 0}
          hint="AI가 걸렀는데 관리자가 허용"
        />
        <StatCard
          label="미탐"
          value={quality.data?.falseNegative ?? 0}
          hint="AI가 놓쳤는데 관리자가 차단"
        />
        <StatCard
          label="판정 일치"
          value={quality.data?.agreed ?? 0}
          hint={`총 ${quality.data?.total ?? 0}건 중`}
        />
      </div>

      <Card>
        <CardHeader
          title="검열 대기"
          description={TABS.find((tab) => tab.value === status)?.hint}
          actions={
            <div className="flex rounded-md border border-border-default p-0.5">
              {TABS.map((tab) => (
                <button
                  key={tab.value}
                  type="button"
                  onClick={() => {
                    setStatus(tab.value);
                    setPage(0);
                  }}
                  className={cn(
                    'rounded px-2.5 py-1 text-xs font-medium transition-colors',
                    status === tab.value
                      ? 'bg-subtle text-ink'
                      : 'text-ink-muted hover:text-ink',
                  )}
                >
                  {tab.label}
                </button>
              ))}
            </div>
          }
        />

        {audits.isError ? (
          <ErrorState message={(audits.error as Error).message} onRetry={() => audits.refetch()} />
        ) : (
          <>
            <DataTable
              columns={columns}
              data={audits.data?.content ?? []}
              loading={audits.isPending}
              emptyTitle={`${status} 상태의 닉네임이 없습니다`}
              emptyDescription="새 닉네임이 검열에 걸리면 여기에 쌓입니다."
            />
            {audits.data && (
              <Pagination
                page={audits.data.page}
                totalPages={audits.data.totalPages}
                totalElements={audits.data.totalElements}
                onChange={setPage}
              />
            )}
          </>
        )}
      </Card>

      {/* 검열 큐 아래에 둔다. 같은 화면인 이유는, 큐를 보다가 "이건 사전에 넣자" 하는
       * 순간이 잦기 때문이다. 메뉴를 옮겨 가며 하면 그 흐름이 끊긴다. */}
      <ProfanityWordsCard />
    </div>
  );
}

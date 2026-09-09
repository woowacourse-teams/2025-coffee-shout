import type { ColumnDef } from '@tanstack/react-table';
import { useMemo, useState } from 'react';
import { useAuditLogs } from '@/api/queries';
import type { AdminAuditLog } from '@/api/types';
import { Card, CardHeader } from '@/components/ui/Card';
import { PageHeader } from '@/components/ui/PageHeader';
import { Pagination } from '@/components/ui/Pagination';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';

export function AuditLogsPage() {
  const [page, setPage] = useState(0);
  const logs = useAuditLogs(30, page);

  const columns = useMemo<ColumnDef<AdminAuditLog, unknown>[]>(
    () => [
      {
        accessorKey: 'createdAt',
        header: '시각',
        meta: { width: '13rem' },
        cell: (c) => <Timestamp value={c.getValue() as string} />,
      },
      {
        accessorKey: 'actorEmail',
        header: '실행자',
        meta: { width: '15rem' },
        cell: (c) => <span className="font-medium">{String(c.getValue())}</span>,
      },
      {
        accessorKey: 'action',
        header: '조치',
        cell: (c) => <span className="font-mono text-xs">{String(c.getValue())}</span>,
      },
      {
        accessorKey: 'targetId',
        header: '대상',
        meta: { width: '10rem' },
        cell: (c) =>
          c.getValue() ? (
            <span className="font-mono text-xs">{String(c.getValue())}</span>
          ) : (
            <span className="text-ink-muted">-</span>
          ),
      },
      {
        accessorKey: 'result',
        header: '결과',
        meta: { width: '7rem' },
        cell: (c) =>
          c.getValue() === 'FAILURE' ? (
            <StatusBadge tone="attention">실패</StatusBadge>
          ) : (
            <StatusBadge tone="muted">성공</StatusBadge>
          ),
      },
      {
        accessorKey: 'detail',
        header: '비고',
        cell: (c) =>
          c.getValue() ? (
            <span className="line-clamp-1 text-xs text-ink-muted">{String(c.getValue())}</span>
          ) : null,
      },
    ],
    [],
  );

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title="조치 이력"
        description="관리자가 상태를 바꾼 모든 요청이 남습니다. 조회는 남기지 않습니다. 목록을 열어본 기록까지 쌓으면 실제 조치가 묻힙니다."
      />

      <Card>
        <CardHeader
          title="감사 로그"
          description="append-only 입니다. 수정도 삭제도 하지 않습니다."
        />
        <DataTable
          error={logs.error}
          onRetry={() => logs.refetch()}
          columns={columns}
          data={logs.data?.content ?? []}
          loading={logs.isPending}
          emptyTitle="조치 이력이 없습니다"
          emptyDescription="관리자가 무언가를 바꾸면 여기에 남습니다."
        />
        {logs.data && (
          <Pagination
            page={logs.data.page}
            totalPages={logs.data.totalPages}
            totalElements={logs.data.totalElements}
            onChange={setPage}
          />
        )}
      </Card>
    </div>
  );
}

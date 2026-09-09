import type { ColumnDef } from '@tanstack/react-table';
import { useMemo, useState } from 'react';
import { useReportSla, useReports, useResolveReport } from '@/api/queries';
import type { Report, ReportStatus } from '@/api/types';
import { Button } from '@/components/ui/Button';
import { Card, CardHeader } from '@/components/ui/Card';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';
import { ErrorState } from '@/components/ui/EmptyState';
import { ERROR_SURFACE } from '@/components/ui/errorSurface';
import { Loaded } from '@/components/ui/Loaded';
import { Select } from '@/components/ui/Field';
import { PageHeader } from '@/components/ui/PageHeader';
import { Pagination } from '@/components/ui/Pagination';
import { Tile } from '@/components/ui/Tile';
import { TileGrid, TileSkeletons } from '@/components/ui/TileGrid';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';
import { formatDurationMinutes } from '@/lib/format';

export function ReportsPage() {
  const [status, setStatus] = useState<ReportStatus | ''>('PENDING');
  const [page, setPage] = useState(0);
  const [target, setTarget] = useState<Report | null>(null);

  const reports = useReports({ status: status || undefined, page });
  const sla = useReportSla(30);
  const resolve = useResolveReport();

  const columns = useMemo<ColumnDef<Report, unknown>[]>(
    () => [
      {
        accessorKey: 'id',
        header: 'ID',
        meta: { width: '4rem' },
        cell: (c) => <span className="font-mono text-xs text-ink-muted">{String(c.getValue())}</span>,
      },
      { accessorKey: 'category', header: '카테고리', meta: { width: '8rem' } },
      {
        accessorKey: 'content',
        header: '내용',
        cell: (c) => <span className="line-clamp-2">{String(c.getValue())}</span>,
      },
      {
        accessorKey: 'joinCode',
        header: '방',
        meta: { width: '6rem' },
        cell: (c) =>
          c.getValue() ? (
            <span className="font-mono text-xs">{String(c.getValue())}</span>
          ) : (
            <span className="text-ink-muted">-</span>
          ),
      },
      {
        accessorKey: 'status',
        header: '상태',
        meta: { width: '7rem' },
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
        meta: { width: '13rem' },
        cell: (c) => <Timestamp value={c.getValue() as string} />,
      },
      {
        id: 'actions',
        header: '',
        meta: { width: '6rem', align: 'right' },
        cell: (c) =>
          c.row.original.status === 'PENDING' ? (
            <Button
              size="sm"
              onClick={(event) => {
                // 행 클릭(드릴다운)과 버튼 클릭이 겹치지 않게 한다.
                event.stopPropagation();
                setTarget(c.row.original);
              }}
            >
              처리
            </Button>
          ) : null,
      },
    ],
    [],
  );

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title="신고"
        description="접수된 신고를 확인하고 처리합니다. 처리 시간은 최근 30일 기준입니다."
      />

      {/* 실패하면 "-" 대신 실패했다고 말한다. "-" 는 "오늘 0건"과 똑같이 생겨서,
        * 서버가 답을 못 준 것을 처리할 게 없는 것으로 읽게 만든다. */}
      <Loaded
        query={sla}
        errorClassName={ERROR_SURFACE}
        skeleton={
          <TileGrid>
            <TileSkeletons />
          </TileGrid>
        }
      >
        {(data) => (
          <TileGrid>
            <Tile label="미처리" value={data.pendingCount} />
            <Tile
              label="가장 오래 기다린 건"
              value={formatDurationMinutes(data.oldestPendingMinutes)}
              hint="접수 후 경과"
            />
            <Tile
              label="처리 시간 중앙값"
              value={formatDurationMinutes(data.p50Minutes)}
              hint="평균이 아닌 중앙값"
            />
            <Tile
              label="처리 시간 p95"
              value={formatDurationMinutes(data.p95Minutes)}
              hint={`최근 30일 ${data.resolvedCount}건 기준`}
            />
          </TileGrid>
        )}
      </Loaded>

      <Card>
        <CardHeader
          title="신고 목록"
          actions={
            <Select
              value={status}
              onChange={(event) => {
                setStatus(event.target.value as ReportStatus | '');
                setPage(0);
              }}
              className="w-32"
            >
              <option value="">전체</option>
              <option value="PENDING">미처리</option>
              <option value="RESOLVED">처리 완료</option>
            </Select>
          }
        />

        {reports.isError ? (
          <ErrorState
            message={(reports.error as Error).message}
            onRetry={() => reports.refetch()}
          />
        ) : (
          <>
            <DataTable
              columns={columns}
              data={reports.data?.content ?? []}
              loading={reports.isPending}
              emptyTitle={
                status === 'PENDING' ? '미처리 신고가 없습니다' : '신고가 없습니다'
              }
              emptyDescription="새 신고가 들어오면 여기에 쌓입니다."
            />
            {reports.data && (
              <Pagination
                page={reports.data.page}
                totalPages={reports.data.totalPages}
                totalElements={reports.data.totalElements}
                onChange={setPage}
              />
            )}
          </>
        )}
      </Card>

      <ConfirmDialog
        open={target !== null}
        onOpenChange={(open) => !open && setTarget(null)}
        destructive={false}
        title="이 신고를 처리 완료로 표시할까요?"
        description="처리 완료로 바꾸면 미처리 목록에서 빠집니다. 되돌리는 기능은 없습니다."
        target={target ? `#${target.id} · ${target.content}` : undefined}
        confirmLabel="처리 완료로 표시"
        pending={resolve.isPending}
        onConfirm={() => {
          if (!target) return;
          resolve.mutate(target.id, { onSuccess: () => setTarget(null) });
        }}
      />
    </div>
  );
}

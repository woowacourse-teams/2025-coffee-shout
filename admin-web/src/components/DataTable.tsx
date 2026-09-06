import {
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  useReactTable,
  type ColumnDef,
  type SortingState,
} from '@tanstack/react-table';
import { ArrowDown, ArrowUp, ChevronsUpDown } from 'lucide-react';
import { useState } from 'react';
import { cn } from '@/lib/cn';
import { EmptyState, Skeleton } from '@/components/ui/EmptyState';

type DataTableProps<T> = {
  columns: ColumnDef<T, unknown>[];
  data: T[];
  loading?: boolean;
  /** 비어 있을 때 문구. 화면마다 다르게 준다. */
  emptyTitle: string;
  emptyDescription?: string;
  /** 행 클릭으로 드릴다운. 주면 커서와 호버가 붙는다. */
  onRowClick?: (row: T) => void;
  /** 표시 행 수. 로딩 스켈레톤을 실제 높이와 맞추는 데 쓴다. */
  skeletonRows?: number;
  className?: string;
};

/**
 * 모든 목록 화면이 쓰는 하나의 표.
 *
 * <p>화면마다 표를 따로 만들면 정렬 표시, 빈 상태, 로딩, 행 높이가 조금씩 달라진다.
 * 운영자는 화면을 옮길 때마다 다시 배워야 하고, 그 차이는 버그처럼 느껴진다.
 *
 * <p>정렬은 클라이언트에서 한다. 한 페이지가 20행이라 서버 왕복이 필요 없고,
 * 페이지 안에서 즉시 뒤집히는 편이 훑기에 낫다.
 */
export function DataTable<T>({
  columns,
  data,
  loading,
  emptyTitle,
  emptyDescription,
  onRowClick,
  skeletonRows = 5,
  className,
}: DataTableProps<T>) {
  const [sorting, setSorting] = useState<SortingState>([]);

  const table = useReactTable({
    data,
    columns,
    state: { sorting },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
  });

  return (
    /* 넓은 표는 자기 컨테이너 안에서 가로 스크롤한다. 페이지가 통째로 밀리면
     * 좌측 레일과 헤더까지 함께 움직여 화면이 무너진다. */
    <div className={cn('overflow-x-auto', className)}>
      <table className="w-full min-w-full border-collapse text-sm">
        <thead>
          {table.getHeaderGroups().map((headerGroup) => (
            <tr key={headerGroup.id} className="border-b border-border-default bg-subtle">
              {headerGroup.headers.map((header) => {
                const sortable = header.column.getCanSort();
                const sorted = header.column.getIsSorted();
                return (
                  <th
                    key={header.id}
                    scope="col"
                    className="px-3 py-2 text-left text-xs font-medium text-ink-secondary"
                  >
                    {header.isPlaceholder ? null : sortable ? (
                      <button
                        type="button"
                        onClick={header.column.getToggleSortingHandler()}
                        className="inline-flex items-center gap-1 hover:text-ink"
                      >
                        {flexRender(header.column.columnDef.header, header.getContext())}
                        {sorted === 'asc' ? (
                          <ArrowUp className="size-3" aria-hidden />
                        ) : sorted === 'desc' ? (
                          <ArrowDown className="size-3" aria-hidden />
                        ) : (
                          <ChevronsUpDown className="size-3 text-ink-muted" aria-hidden />
                        )}
                        <span className="sr-only">
                          {sorted === 'asc' ? '오름차순 정렬됨' : sorted === 'desc' ? '내림차순 정렬됨' : '정렬'}
                        </span>
                      </button>
                    ) : (
                      flexRender(header.column.columnDef.header, header.getContext())
                    )}
                  </th>
                );
              })}
            </tr>
          ))}
        </thead>

        <tbody>
          {loading &&
            Array.from({ length: skeletonRows }).map((_, rowIndex) => (
              <tr key={rowIndex} className="border-b border-border-default">
                {columns.map((_column, columnIndex) => (
                  <td key={columnIndex} className="h-row px-3">
                    <Skeleton className="h-3 w-full max-w-32" />
                  </td>
                ))}
              </tr>
            ))}

          {!loading &&
            table.getRowModel().rows.map((row) => (
              <tr
                key={row.id}
                onClick={onRowClick ? () => onRowClick(row.original) : undefined}
                className={cn(
                  'border-b border-border-default',
                  onRowClick && 'cursor-pointer hover:bg-selected',
                )}
              >
                {row.getVisibleCells().map((cell) => (
                  <td key={cell.id} className="h-row px-3 text-ink">
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </td>
                ))}
              </tr>
            ))}
        </tbody>
      </table>

      {!loading && data.length === 0 && (
        <EmptyState title={emptyTitle} description={emptyDescription} />
      )}
    </div>
  );
}

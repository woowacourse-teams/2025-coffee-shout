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
import { EmptyState, ErrorState, Skeleton } from '@/components/ui/EmptyState';

/**
 * 열 정렬 방향. 숫자 열은 오른쪽으로 붙여야 자릿수가 눈으로 비교된다.
 * `meta: { align: 'right' }` 로 지정한다.
 */
declare module '@tanstack/react-table' {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  interface ColumnMeta<TData, TValue> {
    align?: 'left' | 'right';
    /** 좁게 유지할 열(ID, 상태 등). 내용이 길어져도 늘어나지 않는다. */
    width?: string;
  }
}

type DataTableProps<T> = {
  columns: ColumnDef<T, unknown>[];
  data: T[];
  loading?: boolean;
  /** 비어 있을 때 문구. 화면마다 다르게 준다. */
  emptyTitle: string;
  emptyDescription?: string;
  /** 행 클릭으로 드릴다운. 주면 커서와 호버가 붙는다. */
  onRowClick?: (row: T) => void;
  /**
   * 조회 실패. 주면 표 자리에 실패 문구를 그린다.
   *
   * <p>화면이 {@code error ? <ErrorState/> : <DataTable/>} 로 갈라 쓰던 것을 안으로
   * 들였다. 그렇게 쓰면 실패했을 때 <b>표 머리까지 통째로 사라져</b>, 비었을 때와 실패했을
   * 때가 다른 모양이 된다. 로딩과 빈 상태는 이미 이 컴포넌트가 머리를 남긴 채 그린다.
   */
  error?: unknown;
  onRetry?: () => void;
  /** 로딩 스켈레톤 행 수. 실제 표시 행 수와 맞추면 데이터가 와도 화면이 튀지 않는다. */
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
  error,
  onRetry,
  skeletonRows = 6,
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

  const failed = error != null;
  const showEmpty = !failed && !loading && data.length === 0;
  // 실패했으면 스켈레톤을 돌리지 않는다. 재시도 버튼 위에서 회색 막대가 깜빡이면
  // 아직 불러오는 중인 것처럼 보인다.
  const showSkeleton = loading && !failed;

  return (
    /* 넓은 표는 자기 컨테이너 안에서 가로 스크롤한다. 페이지가 통째로 밀리면
     * 좌측 레일과 헤더까지 함께 움직여 화면이 무너진다.
     *
     * 셀 좌우 여백은 12px 인데 <b>맨 앞뒤 열만 20px</b>이다. 12px 로 통일하면 표의 첫
     * 글자가 카드 제목보다 8px 안쪽에서 시작해 카드가 어긋나 보이고, 20px 로 통일하면
     * 열이 많은 표에서 가로 스크롤이 그만큼 빨리 생긴다. 정렬이 필요한 것은 바깥 모서리뿐이다. */
    <div className={cn('overflow-x-auto', className)}>
      <table className="w-full border-collapse text-sm">
        <thead>
          {table.getHeaderGroups().map((headerGroup) => (
            <tr key={headerGroup.id} className="border-b border-border-default">
              {headerGroup.headers.map((header) => {
                const meta = header.column.columnDef.meta;
                const sortable = header.column.getCanSort();
                const sorted = header.column.getIsSorted();

                return (
                  <th
                    key={header.id}
                    scope="col"
                    style={meta?.width ? { width: meta.width } : undefined}
                    className={cn(
                      'whitespace-nowrap bg-subtle/60 px-3 py-2 text-2xs font-semibold tracking-wide text-ink-muted',
                      'first:pl-5 last:pr-5',
                      meta?.align === 'right' ? 'text-right' : 'text-left',
                    )}
                  >
                    {header.isPlaceholder ? null : sortable ? (
                      <button
                        type="button"
                        onClick={header.column.getToggleSortingHandler()}
                        className={cn(
                          'group/sort inline-flex items-center gap-1 transition-colors hover:text-ink',
                          meta?.align === 'right' && 'flex-row-reverse',
                        )}
                      >
                        {flexRender(header.column.columnDef.header, header.getContext())}
                        {sorted === 'asc' ? (
                          <ArrowUp className="size-3 text-accent" aria-hidden />
                        ) : sorted === 'desc' ? (
                          <ArrowDown className="size-3 text-accent" aria-hidden />
                        ) : (
                          // 정렬 가능한 열임을 늘 알리되, 호버 전에는 눈에 띄지 않게 둔다.
                          <ChevronsUpDown
                            className="size-3 opacity-0 transition-opacity group-hover/sort:opacity-60"
                            aria-hidden
                          />
                        )}
                        <span className="sr-only">
                          {sorted === 'asc'
                            ? '오름차순 정렬됨'
                            : sorted === 'desc'
                              ? '내림차순 정렬됨'
                              : '정렬하려면 누르세요'}
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
          {showSkeleton &&
            Array.from({ length: skeletonRows }).map((_, rowIndex) => (
              <tr key={rowIndex} className="border-b border-border-default">
                {columns.map((_column, columnIndex) => (
                  <td key={columnIndex} className="h-row px-3 first:pl-5 last:pr-5">
                    <Skeleton
                      className="h-3"
                      // 폭을 조금씩 다르게 두면 실제 내용처럼 보여 로딩이 덜 답답하다.
                      style={{ width: `${45 + ((rowIndex * 7 + columnIndex * 13) % 45)}%` }}
                    />
                  </td>
                ))}
              </tr>
            ))}

          {!showSkeleton &&
            !failed &&
            table.getRowModel().rows.map((row) => (
              <tr
                key={row.id}
                onClick={onRowClick ? () => onRowClick(row.original) : undefined}
                className={cn(
                  'border-b border-border-default transition-colors',
                  onRowClick && 'cursor-pointer hover:bg-selected',
                )}
              >
                {row.getVisibleCells().map((cell) => {
                  const meta = cell.column.columnDef.meta;
                  return (
                    <td
                      key={cell.id}
                      className={cn(
                        'h-row px-3 text-ink',
                        'first:pl-5 last:pr-5',
                        meta?.align === 'right' && 'text-right tabular-nums',
                      )}
                    >
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </td>
                  );
                })}
              </tr>
            ))}
        </tbody>
      </table>

      {failed && <ErrorState message={(error as Error)?.message} onRetry={onRetry} />}
      {showEmpty && <EmptyState title={emptyTitle} description={emptyDescription} />}
    </div>
  );
}

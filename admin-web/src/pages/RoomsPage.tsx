import type { ColumnDef } from '@tanstack/react-table';
import { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useRoomSearch } from '@/api/queries';
import type { RoomState, RoomSummary } from '@/api/types';
import { Card, CardHeader } from '@/components/ui/Card';
import { SearchInput } from '@/components/ui/Field';
import { PageHeader } from '@/components/ui/PageHeader';
import { Pagination } from '@/components/ui/Pagination';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';
import { useDebounced } from '@/lib/useDebounced';

/** 방이 어디까지 갔는지. 완주한 방은 물러나고 중간에 멈춘 방이 눈에 걸려야 한다. */
export function roomStatusBadge(status: RoomState) {
  if (status === 'DONE') return <StatusBadge tone="muted">완주</StatusBadge>;
  if (status === 'READY') return <StatusBadge tone="attention">시작 안 함</StatusBadge>;
  return <StatusBadge tone="neutral">{status}</StatusBadge>;
}

export function RoomsPage() {
  const navigate = useNavigate();
  // 다른 화면이 코드를 들고 넘어온다. 신고 패널의 방 링크가 `?joinCode=ABC12` 로 온다.
  // 초기값으로만 읽는다 - 이후 입력은 여기서 하는 것이므로 주소를 계속 따라가면
  // 지우고 다시 치는 동안 주소가 덮어써 버린다.
  const [params] = useSearchParams();
  const [input, setInput] = useState(() => params.get('joinCode') ?? '');
  const [page, setPage] = useState(0);

  // 타이핑마다 서버를 때리면 5글자 코드에 다섯 번 조회한다.
  const joinCode = useDebounced(input, 300);
  const rooms = useRoomSearch(joinCode, page);

  const columns = useMemo<ColumnDef<RoomSummary, unknown>[]>(
    () => [
      {
        accessorKey: 'joinCode',
        header: 'joinCode',
        meta: { width: '8rem' },
        cell: (c) => <span className="font-mono font-medium">{String(c.getValue())}</span>,
      },
      {
        accessorKey: 'status',
        header: '상태',
        meta: { width: '9rem' },
        cell: (c) => roomStatusBadge(c.getValue() as RoomState),
      },
      {
        accessorKey: 'playerCount',
        header: '참여자',
        meta: { width: '6rem', align: 'right' },
      },
      {
        accessorKey: 'createdAt',
        header: '생성',
        meta: { width: '13rem' },
        cell: (c) => <Timestamp value={c.getValue() as string} />,
      },
      {
        accessorKey: 'finishedAt',
        header: '종료',
        meta: { width: '13rem' },
        cell: (c) => <Timestamp value={c.getValue() as string | null} absoluteOnly />,
      },
    ],
    [],
  );

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title="방 조회"
        description="joinCode 로 방을 찾아 참여자, 게임 결과, 룰렛 결과를 확인합니다. 같은 코드가 재사용되므로 결과가 여러 개일 수 있습니다."
      />

      <Card>
        <CardHeader
          title="방 목록"
          actions={
            <SearchInput
              value={input}
              onChange={(event) => {
                setInput(event.target.value);
                setPage(0);
              }}
              placeholder="joinCode (비우면 전체)"
              className="w-56"
            />
          }
        />

        <DataTable
          error={rooms.error}
          onRetry={() => rooms.refetch()}
          columns={columns}
          data={rooms.data?.content ?? []}
          loading={rooms.isPending}
          emptyTitle={joinCode ? `'${joinCode}' 방을 찾지 못했습니다` : '방이 없습니다'}
          emptyDescription={joinCode ? '코드를 다시 확인해주세요.' : undefined}
          onRowClick={(room) => navigate(`/rooms/${room.id}`)}
        />
        {rooms.data && (
          <Pagination
            page={rooms.data.page}
            totalPages={rooms.data.totalPages}
            totalElements={rooms.data.totalElements}
            onChange={setPage}
          />
        )}
      </Card>
    </div>
  );
}

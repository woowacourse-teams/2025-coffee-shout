import type { ColumnDef } from '@tanstack/react-table';
import { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useRoomSearch } from '@/api/queries';
import type { RoomState, RoomSummary } from '@/api/types';
import { Card, CardHeader } from '@/components/ui/Card';
import { SearchInput } from '@/components/ui/Field';
import { PageHeader } from '@/components/ui/PageHeader';
import { Pagination } from '@/components/ui/Pagination';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';
import { RoomPanel } from '@/pages/lookup/RoomPanel';
import { roomStatusBadge } from '@/pages/lookup/roomStatus';
import { useDebounced } from '@/lib/useDebounced';

/**
 * 방 조회. "우리 방 결과가 이상해요" 문의에 답하는 화면이다.
 *
 * <p>한때 유저 조회와 한 화면에 있었다. 단서 하나로 양쪽을 동시에 찾자는 것이었는데,
 * 표 두 개가 세로로 쌓이면서 <b>어느 표를 보고 있는지가 흐려졌고</b> 화면이 3800px 이
 * 됐다. 찾는 대상이 방인지 사람인지는 문의를 받은 시점에 이미 정해져 있다.
 *
 * <p>상세는 라우트가 아니라 패널이다. 화면이 통째로 바뀌면 돌아왔을 때 검색어와 페이지가
 * 초기화되는데, 조사는 한 번에 끝나는 일이 아니라 목록과 상세를 오가는 왕복이다.
 */
export function RoomsPage() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();

  // 주소의 q 는 초기값으로만 읽는다. 신고 패널의 방 링크가 이 값을 들고 온다.
  // 계속 따라가면 지우고 다시 치는 동안 주소가 입력을 덮어쓴다.
  const [input, setInput] = useState(() => params.get('q') ?? '');
  const [page, setPage] = useState(0);

  // 타이핑마다 서버를 때리면 다섯 글자 코드에 다섯 번 조회한다.
  const joinCode = useDebounced(input, 300);
  const rooms = useRoomSearch(joinCode, page);

  const openId = readId(params.get('open'), 'room');

  const open = (roomId: number) => {
    const updated = new URLSearchParams(params);
    updated.set('open', `room:${roomId}`);
    // 갈음한다. 패널을 여닫은 횟수만큼 뒤로가기를 눌러야 목록을 벗어나면 뒤로가기가
    // 쓸모없어진다.
    setParams(updated, { replace: true });
  };

  const close = () => {
    const updated = new URLSearchParams(params);
    updated.delete('open');
    setParams(updated, { replace: true });
  };

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
      { accessorKey: 'playerCount', header: '참여자', meta: { width: '6rem', align: 'right' } },
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
    <div className="flex flex-col gap-4">
      <PageHeader
        title="방"
        description="joinCode 로 방을 찾아 참여자, 게임 결과, 룰렛 결과를 확인합니다. 같은 코드가 재사용되므로 결과가 여러 개일 수 있습니다."
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

      <Card>
        <CardHeader title="방 목록" description="행을 누르면 그 방의 기록이 옆에서 열립니다." />
        <DataTable
          columns={columns}
          data={rooms.data?.content ?? []}
          loading={rooms.isPending}
          error={rooms.error}
          onRetry={() => rooms.refetch()}
          onRowClick={(room) => open(room.id)}
          isRowSelected={(room) => room.id === openId}
          emptyTitle={joinCode ? `'${joinCode}' 방을 찾지 못했습니다` : '방이 없습니다'}
          emptyDescription={joinCode ? '코드를 다시 확인해주세요.' : undefined}
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

      {/* 참여자에서 그 사람으로 건너뛴다. 화면은 바뀌지만 패널은 열린 채로 이어지고,
        * 유저 쪽에서 이 방으로 돌아오는 길이 남는다. 문의는 대개 "이 방의 이 사람"으로
        * 오므로 그 경로가 끊겨 있으면 조사가 매번 검색부터 다시 시작된다. */}
      <RoomPanel
        roomId={openId}
        onClose={close}
        onPivotToUser={(userId) => navigate(`/users?open=user:${userId}&from=room:${openId}`)}
      />
    </div>
  );
}

/** `room:12` 에서 12를 꺼낸다. 종류가 다르거나 숫자가 아니면 열지 않는다. */
export function readId(raw: string | null, kind: string): number | null {
  const [prefix, value] = (raw ?? '').split(':');
  const id = Number(value);
  return prefix === kind && Number.isFinite(id) ? id : null;
}

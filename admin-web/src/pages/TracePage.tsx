import type { ColumnDef } from '@tanstack/react-table';
import { useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useRoomSearch, useUserSearch } from '@/api/queries';
import type { RoomState, RoomSummary, UserSummary } from '@/api/types';
import { Card, CardHeader } from '@/components/ui/Card';
import { SearchInput } from '@/components/ui/Field';
import { PageHeader } from '@/components/ui/PageHeader';
import { Pagination } from '@/components/ui/Pagination';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';
import { RoomPanel } from '@/pages/trace/RoomPanel';
import { roomStatusBadge } from '@/pages/trace/roomStatus';
import { UserPanel } from '@/pages/trace/UserPanel';
import { useDebounced } from '@/lib/useDebounced';

/**
 * 추적. 단서 하나로 방과 유저를 함께 찾는다.
 *
 * <p>예전에는 방 조회와 유저 조회가 다른 화면이었다. 운영자는 문의 내용을 손에 들고
 * 오는데, <b>화면을 먼저 골라야</b> 검색할 수 있었다. "ABC12"가 방 코드인지 유저 코드인지
 * 판단하는 일이 조사보다 앞에 있었다.
 *
 * <p>그래서 입력을 하나로 두고 <b>양쪽을 동시에 조회한다.</b> 종류를 추론하지 않는다.
 * 추론하면 반드시 틀리는 입력이 생기고, 틀렸을 때 운영자는 "없는 방"이라고 읽는다.
 * 결과는 각자의 표로 나온다 - 방은 상태와 참여자 수가, 유저는 닉네임과 가입일이 필요해
 * 한 표에 섞으면 두 쪽 모두 열이 모자라진다.
 *
 * <p>상세는 라우트가 아니라 패널이다. 화면이 통째로 바뀌면 돌아왔을 때 검색어와 페이지가
 * 초기화되는데, 조사는 한 번에 끝나는 일이 아니라 <b>목록과 상세를 오가는 왕복</b>이다.
 */
export function TracePage() {
  const [params, setParams] = useSearchParams();

  // 주소의 q 는 초기값으로만 읽는다. 이후 입력은 여기서 하는 것이라 계속 따라가면
  // 지우고 다시 치는 동안 주소가 입력을 덮어쓴다.
  const [input, setInput] = useState(() => params.get('q') ?? '');
  const [roomPage, setRoomPage] = useState(0);
  const [userPage, setUserPage] = useState(0);

  // 타이핑마다 두 곳을 때리면 다섯 글자에 열 번 조회한다.
  const keyword = useDebounced(input, 300);
  const rooms = useRoomSearch(keyword, roomPage);
  const users = useUserSearch(keyword, userPage);

  const panel = readPanel(params);

  const open = (next: { kind: 'room' | 'user'; id: number; from?: string }) => {
    const updated = new URLSearchParams(params);
    updated.set('open', `${next.kind}:${next.id}`);
    if (next.from) {
      updated.set('from', next.from);
    } else {
      updated.delete('from');
    }
    // 갈음한다. 패널을 여닫은 횟수만큼 뒤로가기를 눌러야 목록을 벗어나면 뒤로가기가
    // 쓸모없어진다. 피벗도 마찬가지다 - 돌아갈 길은 패널 안의 링크가 준다.
    setParams(updated, { replace: true });
  };

  const close = () => {
    const updated = new URLSearchParams(params);
    updated.delete('open');
    updated.delete('from');
    setParams(updated, { replace: true });
  };

  const search = (value: string) => {
    setInput(value);
    setRoomPage(0);
    setUserPage(0);
  };

  const roomColumns = useMemo<ColumnDef<RoomSummary, unknown>[]>(
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

  const userColumns = useMemo<ColumnDef<UserSummary, unknown>[]>(
    () => [
      {
        accessorKey: 'userCode',
        header: '유저코드',
        meta: { width: '8rem' },
        cell: (c) => <span className="font-mono font-medium">{String(c.getValue())}</span>,
      },
      {
        accessorKey: 'nickname',
        header: '닉네임',
        cell: (c) =>
          c.getValue() ? String(c.getValue()) : <span className="text-ink-muted">(없음)</span>,
      },
      {
        accessorKey: 'createdAt',
        header: '가입',
        meta: { width: '13rem' },
        cell: (c) => <Timestamp value={c.getValue() as string} />,
      },
    ],
    [],
  );

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title="추적"
        description="joinCode, 유저코드, 닉네임 중 무엇이든 넣으면 방과 유저를 함께 찾습니다."
        actions={
          <SearchInput
            value={input}
            onChange={(event) => search(event.target.value)}
            placeholder="단서 (비우면 전체)"
            className="w-72"
          />
        }
      />

      <Card>
        <CardHeader
          title="방"
          description="같은 코드가 재사용되므로 결과가 여러 개일 수 있습니다."
        />
        <DataTable
          columns={roomColumns}
          data={rooms.data?.content ?? []}
          loading={rooms.isPending}
          error={rooms.error}
          onRetry={() => rooms.refetch()}
          onRowClick={(room) => open({ kind: 'room', id: room.id })}
          isRowSelected={(room) => panel.kind === 'room' && panel.id === room.id}
          emptyTitle={keyword ? `'${keyword}' 방을 찾지 못했습니다` : '방이 없습니다'}
          emptyDescription={keyword ? '유저 쪽 결과를 확인해 보세요.' : undefined}
        />
        {rooms.data && (
          <Pagination
            page={rooms.data.page}
            totalPages={rooms.data.totalPages}
            totalElements={rooms.data.totalElements}
            onChange={setRoomPage}
          />
        )}
      </Card>

      <Card>
        <CardHeader title="유저" description="닉네임은 부분 일치, 유저코드는 정확히 일치입니다." />
        <DataTable
          columns={userColumns}
          data={users.data?.content ?? []}
          loading={users.isPending}
          error={users.error}
          onRetry={() => users.refetch()}
          onRowClick={(user) => open({ kind: 'user', id: user.id })}
          isRowSelected={(user) => panel.kind === 'user' && panel.id === user.id}
          emptyTitle={keyword ? `'${keyword}' 유저를 찾지 못했습니다` : '유저가 없습니다'}
          emptyDescription={keyword ? '방 쪽 결과를 확인해 보세요.' : undefined}
        />
        {users.data && (
          <Pagination
            page={users.data.page}
            totalPages={users.data.totalPages}
            totalElements={users.data.totalElements}
            onChange={setUserPage}
          />
        )}
      </Card>

      <RoomPanel
        roomId={panel.kind === 'room' ? panel.id : null}
        onClose={close}
        onPivotToUser={(userId) =>
          open({ kind: 'user', id: userId, from: `room:${panel.id}` })
        }
      />

      <UserPanel
        userId={panel.kind === 'user' ? panel.id : null}
        onBack={
          panel.fromRoomId === null
            ? null
            : () => open({ kind: 'room', id: panel.fromRoomId as number })
        }
        onClose={close}
      />
    </div>
  );
}

/**
 * 주소에서 열린 패널을 읽는다.
 *
 * <p>{@code ?open=room:12} 와 {@code ?open=user:5&from=room:12} 두 가지다. 어디서 왔는지를
 * 주소에 남기는 이유는, 방에서 사람으로 건너간 화면을 새로고침하거나 링크로 넘겨도
 * 돌아갈 길이 살아 있어야 하기 때문이다. 컴포넌트 상태로만 들고 있으면 둘 다 잃는다.
 */
function readPanel(params: URLSearchParams) {
  const open = params.get('open') ?? '';
  const from = params.get('from') ?? '';

  const [kind, rawId] = open.split(':');
  const id = Number(rawId);
  const [fromKind, rawFromId] = from.split(':');
  const fromRoomId = fromKind === 'room' && rawFromId ? Number(rawFromId) : null;

  return {
    kind: (kind === 'room' || kind === 'user') && Number.isFinite(id) ? kind : null,
    id: Number.isFinite(id) ? id : null,
    fromRoomId,
  };
}

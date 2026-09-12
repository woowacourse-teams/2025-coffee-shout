import type { ColumnDef } from '@tanstack/react-table';
import { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useUserSearch } from '@/api/queries';
import type { UserSummary } from '@/api/types';
import { Card, CardHeader } from '@/components/ui/Card';
import { SearchInput } from '@/components/ui/Field';
import { PageHeader } from '@/components/ui/PageHeader';
import { Pagination } from '@/components/ui/Pagination';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';
import { readId } from '@/pages/RoomsPage';
import { UserPanel } from '@/pages/lookup/UserPanel';
import { useDebounced } from '@/lib/useDebounced';

/**
 * 유저 조회. 문의가 들어온 사람을 닉네임이나 유저코드로 찾는 자리다.
 *
 * <p>방에서 건너온 경우 주소에 {@code from=room:12} 가 붙는다. 조사는 방에서 사람으로
 * 갔다가 다시 방으로 오는 왕복이라, 되돌아갈 길이 없으면 목록에서 그 방을 다시 찾아야 한다.
 */
export function UsersPage() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();

  const [input, setInput] = useState(() => params.get('q') ?? '');
  const [page, setPage] = useState(0);

  const keyword = useDebounced(input, 300);
  const users = useUserSearch(keyword, page);

  const openId = readId(params.get('open'), 'user');
  const fromRoomId = readId(params.get('from'), 'room');

  const open = (userId: number) => {
    const updated = new URLSearchParams(params);
    updated.set('open', `user:${userId}`);
    // 목록에서 직접 연 것이라 방에서 왔다는 자취는 지운다. 남겨 두면 엉뚱한 방으로
    // 돌아가는 링크가 뜬다.
    updated.delete('from');
    setParams(updated, { replace: true });
  };

  const close = () => {
    const updated = new URLSearchParams(params);
    updated.delete('open');
    updated.delete('from');
    setParams(updated, { replace: true });
  };

  const columns = useMemo<ColumnDef<UserSummary, unknown>[]>(
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
    <div className="flex flex-col gap-4">
      <PageHeader
        title="유저"
        description="닉네임은 부분 일치, 유저코드는 정확히 일치입니다."
        actions={
          <SearchInput
            value={input}
            onChange={(event) => {
              setInput(event.target.value);
              setPage(0);
            }}
            placeholder="닉네임 또는 유저코드"
            className="w-56"
          />
        }
      />

      <Card>
        <CardHeader title="유저 목록" description="행을 누르면 활동 기록이 옆에서 열립니다." />
        <DataTable
          columns={columns}
          data={users.data?.content ?? []}
          loading={users.isPending}
          error={users.error}
          onRetry={() => users.refetch()}
          onRowClick={(user) => open(user.id)}
          isRowSelected={(user) => user.id === openId}
          emptyTitle={keyword ? `'${keyword}' 유저를 찾지 못했습니다` : '유저가 없습니다'}
          emptyDescription={keyword ? '닉네임 일부만 넣어도 찾습니다.' : undefined}
        />
        {users.data && (
          <Pagination
            page={users.data.page}
            totalPages={users.data.totalPages}
            totalElements={users.data.totalElements}
            onChange={setPage}
          />
        )}
      </Card>

      <UserPanel
        userId={openId}
        onBack={fromRoomId === null ? null : () => navigate(`/rooms?open=room:${fromRoomId}`)}
        onClose={close}
      />
    </div>
  );
}

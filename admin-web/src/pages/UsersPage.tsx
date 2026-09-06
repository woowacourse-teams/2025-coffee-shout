import type { ColumnDef } from '@tanstack/react-table';
import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUserSearch } from '@/api/queries';
import type { UserSummary } from '@/api/types';
import { Card, CardHeader } from '@/components/ui/Card';
import { ErrorState } from '@/components/ui/EmptyState';
import { SearchInput } from '@/components/ui/Field';
import { PageHeader } from '@/components/ui/PageHeader';
import { Pagination } from '@/components/ui/Pagination';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';
import { useDebounced } from '@/lib/useDebounced';

export function UsersPage() {
  const navigate = useNavigate();
  const [input, setInput] = useState('');
  const [page, setPage] = useState(0);

  const keyword = useDebounced(input, 300);
  const users = useUserSearch(keyword, page);

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
          c.getValue() ? (
            String(c.getValue())
          ) : (
            <span className="text-ink-muted">(없음)</span>
          ),
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
        title="유저"
        description="닉네임 부분 일치 또는 유저코드 완전 일치로 찾습니다. 탈퇴 회원은 조회되지 않습니다."
      />

      <Card>
        <CardHeader
          title="유저 목록"
          description="이메일은 암호화 저장이라 백오피스에 표시하지 않습니다."
          actions={
            <SearchInput
              value={input}
              onChange={(event) => {
                setInput(event.target.value);
                setPage(0);
              }}
              placeholder="닉네임 또는 유저코드"
              className="w-60"
            />
          }
        />

        {users.isError ? (
          <ErrorState message={(users.error as Error).message} onRetry={() => users.refetch()} />
        ) : (
          <>
            <DataTable
              columns={columns}
              data={users.data?.content ?? []}
              loading={users.isPending}
              emptyTitle={keyword ? `'${keyword}' 유저를 찾지 못했습니다` : '유저가 없습니다'}
              emptyDescription={
                keyword ? '유저코드는 5자 전체를 정확히 입력해야 합니다.' : undefined
              }
              onRowClick={(user) => navigate(`/users/${user.id}`)}
            />
            {users.data && (
              <Pagination
                page={users.data.page}
                totalPages={users.data.totalPages}
                totalElements={users.data.totalElements}
                onChange={setPage}
              />
            )}
          </>
        )}
      </Card>
    </div>
  );
}

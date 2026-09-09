import type { ColumnDef } from '@tanstack/react-table';
import { lazy, Suspense, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useProviderStats, useUserSearch } from '@/api/queries';
import type { UserSummary } from '@/api/types';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { Skeleton } from '@/components/ui/EmptyState';
import { Loaded } from '@/components/ui/Loaded';
import { SearchInput } from '@/components/ui/Field';
import { PageHeader } from '@/components/ui/PageHeader';
import { Pagination } from '@/components/ui/Pagination';
import { Timestamp } from '@/components/ui/Timestamp';
import { DataTable } from '@/components/DataTable';
import { useDebounced } from '@/lib/useDebounced';

/** recharts 는 무겁다. 유저 화면은 검색하러 들어오는 일이 많아 표가 먼저 떠야 한다. */
const ProviderDonut = lazy(() =>
  import('@/components/ProviderDonut').then((module) => ({ default: module.ProviderDonut })),
);

export function UsersPage() {
  const navigate = useNavigate();
  const [input, setInput] = useState('');
  const [page, setPage] = useState(0);

  const keyword = useDebounced(input, 300);
  const users = useUserSearch(keyword, page);
  const providers = useProviderStats();

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

      {/* 검색창 위에 둔다. 이 화면에 들어오는 이유는 대개 특정 유저를 찾는 것이지만,
        * 목록만 있으면 화면이 검색 상자 하나로 끝난다. 제공자 분포는 그 자리에서
        * 공짜로 답할 수 있는 질문이고, "카카오만 쓰는 것 아닌가" 같은 짐작을 숫자로
        * 바꿔 놓는다. */}
      <Card>
        <CardHeader
          title="소셜 제공자"
          description="회원이 어느 소셜로 들어오는지. 탈퇴 회원의 연결은 빠집니다."
        />
        <CardBody>
          <Loaded query={providers} skeleton={<Skeleton className="h-[200px]" />}>
            {(data) => (
              <Suspense fallback={<Skeleton className="h-[200px]" />}>
                <ProviderDonut stats={data} />
              </Suspense>
            )}
          </Loaded>
        </CardBody>
      </Card>

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

        <DataTable
          error={users.error}
          onRetry={() => users.refetch()}
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
      </Card>
    </div>
  );
}

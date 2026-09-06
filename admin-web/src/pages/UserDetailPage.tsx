import { ArrowLeft } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { useUserDetail } from '@/api/queries';
import { Button } from '@/components/ui/Button';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { ErrorState, Skeleton } from '@/components/ui/EmptyState';
import { KeyValue } from '@/components/ui/KeyValue';
import { PageHeader } from '@/components/ui/PageHeader';
import { StatCard } from '@/components/StatCard';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Timestamp } from '@/components/ui/Timestamp';
import { formatPercent } from '@/lib/format';

export function UserDetailPage() {
  const { userId } = useParams();
  const detail = useUserDetail(Number(userId));

  if (detail.isError) {
    return (
      <Card>
        <ErrorState message={(detail.error as Error).message} onRetry={() => detail.refetch()} />
      </Card>
    );
  }

  if (detail.isPending || !detail.data) {
    return (
      <div className="flex flex-col gap-4">
        <Skeleton className="h-7 w-40" />
        <Skeleton className="h-40 rounded-lg" />
      </div>
    );
  }

  const { summary, providers, roomCount, winCount, winRate } = detail.data;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Button asChild variant="ghost" size="sm" className="-ml-2 mb-2">
          <Link to="/users">
            <ArrowLeft />
            유저 목록
          </Link>
        </Button>
        <PageHeader
          title={summary.nickname ?? summary.userCode}
          description={`유저 #${summary.id}`}
        />
      </div>

      <div className="grid grid-cols-2 gap-3 xl:grid-cols-3">
        <StatCard label="참여한 방" value={roomCount} />
        <StatCard label="당첨" value={winCount} />
        <StatCard
          label="당첨 비율"
          value={formatPercent(winRate)}
          hint="참여 대비. 인원 수가 방마다 달라 기대값도 방마다 다르다"
        />
      </div>

      <Card>
        <CardHeader title="계정" />
        <CardBody>
          <KeyValue
            items={[
              { label: '유저코드', value: <span className="font-mono">{summary.userCode}</span> },
              { label: '닉네임', value: summary.nickname },
              { label: '가입', value: <Timestamp value={summary.createdAt} /> },
              {
                label: '소셜 제공자',
                value:
                  providers.length === 0 ? null : (
                    <span className="flex flex-wrap gap-1.5">
                      {providers.map((provider) => (
                        <StatusBadge key={provider} tone="neutral">
                          {provider}
                        </StatusBadge>
                      ))}
                    </span>
                  ),
              },
            ]}
          />
        </CardBody>
      </Card>
    </div>
  );
}

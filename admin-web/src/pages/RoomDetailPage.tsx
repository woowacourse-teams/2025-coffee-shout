import { ArrowLeft, Trophy } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { useRoomDetail } from '@/api/queries';
import { Button } from '@/components/ui/Button';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { EmptyState, ErrorState, Skeleton } from '@/components/ui/EmptyState';
import { KeyValue } from '@/components/ui/KeyValue';
import { PageHeader } from '@/components/ui/PageHeader';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Timestamp } from '@/components/ui/Timestamp';
import { roomStatusBadge } from '@/pages/RoomsPage';
import { formatPercent } from '@/lib/format';

/**
 * 방 상세. "우리 방 결과가 이상해요" 문의에 답하는 화면이다.
 *
 * <p>Grafana 는 집계만 보여주므로 개별 방에서 누가 어떤 점수를 냈고 최종 확률이 얼마였는지는
 * 여기서만 확인할 수 있다.
 */
export function RoomDetailPage() {
  const { roomId } = useParams();
  const detail = useRoomDetail(Number(roomId));

  if (detail.isError) {
    return (
      <Card>
        <ErrorState message={(detail.error as Error).message} onRetry={() => detail.refetch()} />
      </Card>
    );
  }

  if (detail.isPending || !detail.data) {
    return (
      <div className="flex flex-col gap-6">
        <Skeleton className="h-7 w-40" />
        <Skeleton className="h-40 rounded-lg" />
      </div>
    );
  }

  const { summary, players, miniGameResults, roulette } = detail.data;

  // 게임별로 묶는다. 한 방에서 여러 게임을 하므로 평평하게 나열하면 어느 게임의
  // 결과인지 매 행마다 확인해야 한다.
  const byGame = miniGameResults.reduce<Record<string, typeof miniGameResults>>((acc, result) => {
    (acc[result.miniGameType] ??= []).push(result);
    return acc;
  }, {});

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Button asChild variant="ghost" size="sm" className="-ml-2 mb-2">
          <Link to="/rooms">
            <ArrowLeft />
            방 목록
          </Link>
        </Button>
        <PageHeader
          title={summary.joinCode}
          description={`방 #${summary.id}`}
          actions={roomStatusBadge(summary.status)}
        />
      </div>

      <Card>
        <CardHeader title="개요" />
        <CardBody>
          <KeyValue
            items={[
              { label: 'joinCode', value: <span className="font-mono">{summary.joinCode}</span> },
              { label: '참여자', value: `${summary.playerCount}명` },
              { label: '생성', value: <Timestamp value={summary.createdAt} /> },
              { label: '종료', value: summary.finishedAt ? <Timestamp value={summary.finishedAt} /> : null },
            ]}
          />
        </CardBody>
      </Card>

      <Card>
        <CardHeader title="룰렛 결과" description="당첨 확률은 미니게임 결과로 조정된 최종값입니다." />
        {roulette ? (
          <CardBody className="flex items-center gap-4">
            <span className="flex size-10 shrink-0 items-center justify-center rounded-md bg-attention-solid text-attention-icon-on-solid">
              <Trophy className="size-5" aria-hidden />
            </span>
            <div>
              <p className="text-2xl font-bold leading-none tracking-metric text-ink">
                {roulette.winnerPlayerName}
              </p>
              <p className="mt-0.5 text-xs text-ink-muted">
                당첨 확률 {formatPercent(roulette.winnerProbability / 100, 0)}
                <span className="mx-1.5 text-border-strong">·</span>
                <Timestamp value={roulette.createdAt} absoluteOnly />
              </p>
            </div>
          </CardBody>
        ) : (
          <EmptyState
            title="룰렛까지 가지 않았습니다"
            description="중간에 방이 끝났습니다. 정상적인 경우입니다."
          />
        )}
      </Card>

      <Card>
        <CardHeader title="참여자" description={`${players.length}명`} />
        {players.length === 0 ? (
          <EmptyState title="참여자 기록이 없습니다" />
        ) : (
          <ul className="divide-y divide-border-default">
            {players.map((player) => (
              <li key={player.id} className="flex items-center gap-3 px-5 py-2.5">
                <span className="w-40 truncate font-medium text-ink">{player.playerName}</span>
                {player.playerType === 'HOST' && <StatusBadge tone="neutral">방장</StatusBadge>}
                {player.guest ? (
                  <StatusBadge tone="muted">게스트</StatusBadge>
                ) : (
                  <span className="text-xs text-ink-muted">
                    {player.nickname}
                    <span className="ml-1.5 font-mono">{player.userCode}</span>
                  </span>
                )}
                <Timestamp value={player.joinedAt} absoluteOnly className="ml-auto text-ink-muted" />
              </li>
            ))}
          </ul>
        )}
      </Card>

      <Card>
        <CardHeader title="미니게임 결과" />
        {miniGameResults.length === 0 ? (
          <EmptyState
            title="게임 결과가 없습니다"
            description="게임을 시작하지 않았거나 끝내지 않았습니다."
          />
        ) : (
          <CardBody className="flex flex-col gap-5">
            {Object.entries(byGame).map(([gameType, results]) => (
              <div key={gameType}>
                <p className="mb-1.5 text-xs font-semibold text-ink-secondary">{gameType}</p>
                <ol className="divide-y divide-border-default rounded-md border border-border-default">
                  {results
                    .slice()
                    .sort((a, b) => a.rank - b.rank)
                    .map((result) => (
                      <li
                        key={`${result.playerId}-${result.createdAt}`}
                        className="flex items-center gap-3 px-3 py-2"
                      >
                        <span className="w-6 text-center font-mono text-xs text-ink-muted">
                          {result.rank}
                        </span>
                        <span className="flex-1 truncate">{result.playerName}</span>
                        <span className="tabular-nums text-ink-secondary">
                          {result.score ?? '-'}
                        </span>
                      </li>
                    ))}
                </ol>
              </div>
            ))}
          </CardBody>
        )}
      </Card>
    </div>
  );
}

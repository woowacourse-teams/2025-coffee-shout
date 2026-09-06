import type { GamePlayStat } from '@/api/types';
import { EmptyState } from '@/components/ui/EmptyState';
import { formatNumber, formatPercent } from '@/lib/format';

const GAME_LABEL: Record<string, string> = {
  RACING_GAME: '레이싱',
  BLOCK_STACKING: '블록쌓기',
  SPEED_TOUCH: '스피드터치',
  BLIND_TIMER: '블라인드타이머',
  NUNCHI_GAME: '눈치게임',
  CARD_GAME: '카드게임',
  WORM_GAME: '지렁이',
  LADDER: '사다리',
};

/**
 * 게임별 비중. "블록쌓기를 아무도 안 고른다" 같은 사실은 이 목록으로만 보인다.
 * Grafana 는 게임 타입을 모른다.
 *
 * <p>막대는 <b>1위 대비</b> 길이다. 전체 대비로 그리면 게임이 여덟 개일 때 모든 막대가
 * 짧아져 서로 비교가 안 된다. 비율 숫자는 따로 적으므로 막대는 순위 비교만 맡는다.
 */
export function GameShareList({ stats }: { stats: GamePlayStat[] }) {
  if (stats.length === 0) {
    return (
      <EmptyState
        title="완료된 게임이 없습니다"
        description="게임이 끝나야 집계됩니다. 시작만 하고 만 게임은 기록이 남지 않습니다."
      />
    );
  }

  const top = stats[0]?.plays ?? 1;

  return (
    <ul className="flex flex-col gap-2.5 p-4">
      {stats.map((stat) => (
        <li key={stat.miniGameType} className="grid grid-cols-[6rem_1fr_auto] items-center gap-3">
          <span className="truncate text-xs text-ink-secondary">
            {GAME_LABEL[stat.miniGameType] ?? stat.miniGameType}
          </span>
          <span className="h-2 overflow-hidden rounded-full bg-subtle">
            <span
              className="block h-full rounded-full bg-accent"
              style={{ width: `${Math.max((stat.plays / top) * 100, 2)}%` }}
              aria-hidden
            />
          </span>
          <span className="w-24 text-right text-xs tabular-nums">
            <span className="font-medium text-ink">{formatNumber(stat.plays)}</span>
            <span className="ml-1.5 text-ink-muted">{formatPercent(stat.share, 0)}</span>
          </span>
        </li>
      ))}
    </ul>
  );
}

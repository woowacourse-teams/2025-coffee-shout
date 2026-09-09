import type { GamePlayStat } from '@/api/types';
import { EmptyState } from '@/components/ui/EmptyState';
import { Meter } from '@/components/ui/Meter';
import { cn } from '@/lib/cn';
import { formatNumber, formatPercent } from '@/lib/format';

/**
 * 게임별 비중. "블록쌓기를 아무도 안 고른다" 같은 사실은 이 목록으로만 보인다.
 * Grafana 는 게임 타입을 모른다.
 *
 * <p>한글 이름은 서버가 준다. 프론트가 enum 이름과의 대응표를 따로 들고 있었는데,
 * 게임이 늘어나자 빠진 항목이 영문 그대로 노출됐다(LADDER_GAME). 같은 사실을 두 곳에
 * 적어 두면 한쪽만 고치는 날이 온다.
 *
 * <h2>왜 도넛이 아니라 순위 막대인가</h2>
 *
 * <p>비중 합이 100%라 원그래프가 당긴다. 그런데 이 화면에서 실제로 묻는 것은 "1위가
 * 전체의 몇 퍼센트인가"가 아니라 <b>"꼴찌가 누구고 얼마나 안 쓰이는가"</b>다. 게임을
 * 목록에서 뺄지 정하는 자리이기 때문이다. 원그래프는 작은 조각들을 서로 비교하지 못한다.
 * 게임이 여덟 개면 아래쪽 넷은 전부 얇은 부채꼴이 되어 순서조차 안 보인다.
 * 항목이 셋뿐인 소셜 제공자 분포는 반대 이유로 도넛이다.
 *
 * <p>막대는 <b>1위 대비</b> 길이다. 전체 대비로 그리면 게임이 여덟 개일 때 모든 막대가
 * 짧아져 서로 비교가 안 된다. 전체 대비 비율은 숫자로 따로 적으므로, 막대는 순위 비교만
 * 맡으면 된다.
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
    <ul className="flex flex-col px-5 pb-5">
      {stats.map((stat, index) => (
        <li
          key={stat.miniGameType}
          className="grid grid-cols-[1.25rem_1fr] items-center gap-x-2.5 gap-y-1 py-1.5"
        >
          {/* 순위 숫자. 목록이 이미 정렬돼 있어도 숫자가 있어야 "3위와 4위가 붙어 있다"를
            * 말로 옮길 수 있다. 1위만 진하게 둔다. */}
          <span
            className={cn(
              'text-2xs font-semibold tabular-nums',
              index === 0 ? 'text-ink-secondary' : 'text-ink-muted',
            )}
          >
            {index + 1}
          </span>

          <div className="flex min-w-0 items-baseline justify-between gap-3">
            <span className="truncate text-xs text-ink" title={stat.label}>
              {stat.label}
            </span>
            <span className="shrink-0 text-xs tabular-nums">
              <span className="font-semibold text-ink">{formatNumber(stat.plays)}</span>
              <span className="ml-1.5 text-ink-muted">{formatPercent(stat.share, 0)}</span>
            </span>
          </div>

          {/* 막대는 이름 아래 전체 폭을 쓴다. 이름과 한 줄에 두었을 때는 긴 게임 이름
            * 하나가 막대 자리를 잡아먹어 줄마다 막대 시작점이 달랐다. */}
          <Meter
            ratio={top === 0 ? 0 : stat.plays / top}
            size="sm"
            muted={index > 0}
            className="col-start-2"
          />
        </li>
      ))}
    </ul>
  );
}

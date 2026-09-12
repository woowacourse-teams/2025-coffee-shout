import { Link } from 'react-router-dom';
import type { LucideIcon } from 'lucide-react';
import { cn } from '@/lib/cn';
import { formatNumber } from '@/lib/format';

export type QueueItem = {
  label: string;
  count: number;
  to: string;
  icon: LucideIcon;
  /** 평소 0이고 0이 아닌 순간이 곧 사고인 것. 이 줄에만 색이 붙는다. */
  critical?: boolean;
};

/**
 * 처리 대기 줄.
 *
 * <p>다섯 칸을 <b>판 하나에 세로로</b> 쌓는다. 한때 가로로 다섯 장의 카드를 늘어놓았는데,
 * 화면 폭 전체를 쓰면서 각 칸 안이 비었고 홈에서 가장 큰 덩어리가 되어 정작 그 옆에
 * 무엇을 둬도 눌렸다. 세로로 세우면 좁은 열에 들어가고, 다섯 숫자가 오른쪽 끝에 정렬돼
 * <b>어디가 많이 밀렸는지</b>가 한눈에 비교된다. 가로로 늘어놓으면 그 비교를 못 한다.
 *
 * <p>칸막이 선을 두지 않는다. 줄마다 선을 그으면 다섯 줄이 다섯 개의 다른 것으로 보이는데,
 * 이것들은 "지금 밀린 일"이라는 한 덩어리다.
 *
 * <p>색은 격리 메시지에만 붙는다. 신고와 검열은 평소에도 쌓이는 것이 정상이라 늘 코랄이면
 * 그 색이 뜻을 잃는다. 격리는 평소 0이고, 회색이던 자리가 코랄로 <b>바뀌는</b> 것이 신호다.
 */
export function ActionQueueStrip({ items }: { items: QueueItem[] }) {
  return (
    <ul className="flex flex-col px-2 pb-2">
      {items.map((item) => (
        <QueueRow key={item.label} item={item} />
      ))}
    </ul>
  );
}

function QueueRow({ item }: { item: QueueItem }) {
  const idle = item.count === 0;
  const alarming = item.critical === true && !idle;

  return (
    <li>
      <Link
        to={item.to}
        className="flex items-center gap-2.5 rounded-md px-3 py-2.5 transition-colors hover:bg-canvas"
      >
        <item.icon
          className={cn('size-4 shrink-0', alarming ? 'text-attention-mark' : 'text-ink-muted')}
          strokeWidth={2}
          aria-hidden
        />
        <span className="min-w-0 flex-1 truncate text-sm text-ink-secondary" title={item.label}>
          {item.label}
        </span>
        {/* 0은 흐리게 둔다. 지울 수는 없다 - 칸이 사라지면 그 큐가 없어진 것처럼 보이고,
          * 다섯 줄의 자리가 매번 바뀌면 늘 보던 자리에서 숫자를 찾지 못한다. */}
        <span
          className={cn(
            'shrink-0 text-xl font-bold leading-none tracking-metric tabular-nums',
            idle ? 'text-ink-muted' : 'text-ink',
          )}
        >
          {formatNumber(item.count)}
        </span>
      </Link>
    </li>
  );
}

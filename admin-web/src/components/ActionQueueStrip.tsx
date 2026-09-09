import { Link } from 'react-router-dom';
import type { LucideIcon } from 'lucide-react';
import { cn } from '@/lib/cn';
import { formatNumber } from '@/lib/format';

export type QueueItem = {
  label: string;
  count: number;
  to: string;
  icon: LucideIcon;
  /**
   * 시스템이 멈춘 것이라 사람이 판단해 줄 일과 급이 다른 항목.
   * 격리 메시지 하나뿐이다. 0이 아닐 때만 표시가 붙는다.
   */
  critical?: boolean;
};

/**
 * 처리 대기 줄. 홈 화면 맨 위에 다섯 칸이 놓인다.
 *
 * <h2>세 번 갈아엎고 돌아온 자리</h2>
 *
 * <p>① 낱장 카드 다섯에 코랄 테두리와 코랄 아이콘 칩을 붙였다. 이 큐가 전부 0인 날은
 * 거의 없어서 <b>다섯 칸이 전부 빨갰다.</b> 전부 강조하면 강조가 아니다.
 *
 * <p>② 판 하나에 칸을 나누고 격리 메시지 칸만 옅게 물들였다. 흰 판 안에 분홍 블록 하나가
 * 떠 있는 모양이 됐다. 칸을 나눈 판은 <b>표</b>처럼 보이고, 그 안의 색면은 표에 잘못
 * 칠해진 셀처럼 보인다.
 *
 * <p>③ 지금이다. 다시 낱장 카드로 돌아오되 <b>장식을 전부 뺐다.</b> 카드마다 라벨과
 * 숫자만 있고, 아이콘은 모서리에서 흐리게 자리를 지킨다. 강조는 색면이 아니라 <b>라벨 앞의
 * 점 하나</b>가 진다.
 *
 * <p>배운 것은 하나다. 강조를 <b>더 크게</b> 만들려 할수록 화면이 나빠졌다. 나머지를
 * 조용하게 만들면 점 하나로도 충분하다.
 *
 * <h2>숫자가 라벨 위에 온다</h2>
 *
 * <p>이 줄에서 운영자가 하는 일은 <b>0이 아닌 칸을 찾는 것</b>이다. 다섯 라벨을 읽는 것이
 * 아니다. 숫자를 위로 올리면 다섯 숫자가 같은 높이의 한 줄에 서서 좌우 한 번에 훑힌다.
 * 라벨은 눈이 멈춘 칸에서만 읽는다.
 */
export function ActionQueueStrip({ items }: { items: QueueItem[] }) {
  return (
    <div className="grid grid-cols-2 gap-3 lg:grid-cols-5">
      {items.map((item) => (
        <QueueCell key={item.label} item={item} />
      ))}
    </div>
  );
}

function QueueCell({ item }: { item: QueueItem }) {
  const idle = item.count === 0;
  const alarming = item.critical === true && !idle;

  return (
    <Link
      to={item.to}
      className={cn(
        'group flex flex-col gap-1.5 rounded-lg border border-border-default bg-surface px-5 py-4 shadow-card transition-all duration-150',
        'hover:-translate-y-px hover:border-border-strong hover:shadow-popover active:translate-y-0',
      )}
    >
      <span className="flex items-start justify-between gap-2">
        <span
          className={cn(
            'text-xl font-bold leading-none tracking-metric',
            idle ? 'text-ink-muted' : 'text-ink',
          )}
        >
          {formatNumber(item.count)}
        </span>
        {/* 아이콘은 모서리에서 흐리게 자리만 지킨다. 칩으로 감싸면 28px 색면이 되어
          * 그 자체가 강조가 되는데, 여기서 강조해야 할 것은 숫자다. */}
        <item.icon className="size-4 shrink-0 text-ink-muted/70" strokeWidth={2} aria-hidden />
      </span>

      <span className="flex min-w-0 items-center gap-1.5">
        {/* 격리 메시지가 쌓였을 때만 점이 선다. 카드 전체를 물들이는 대신 6px 짜리 점
          * 하나면, 나머지 넷이 조용하므로 눈이 여기서 먼저 멈춘다. */}
        {alarming && <span className="size-1.5 shrink-0 rounded-full bg-accent" aria-hidden />}
        <span className="truncate text-xs text-ink-secondary" title={item.label}>
          {item.label}
        </span>
      </span>
    </Link>
  );
}

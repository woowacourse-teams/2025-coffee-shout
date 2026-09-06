import { Link } from 'react-router-dom';
import { ArrowUpRight, type LucideIcon } from 'lucide-react';
import { cn } from '@/lib/cn';
import { formatNumber } from '@/lib/format';

type QueueCardProps = {
  label: string;
  count: number;
  /** 이 값을 넘으면 위험으로 표시한다. 넘지 않아도 0이 아니면 강조는 된다. */
  threshold?: number;
  to: string;
  icon: LucideIcon;
};

type Level = 'idle' | 'active' | 'critical';

/**
 * 처리 대기 큐 한 칸. 홈 화면 최상단에 네 개가 놓인다.
 *
 * <p>3단계로 나뉜다. <b>0이면 가라앉고, 1 이상이면 강조되고, 임계를 넘으면 위험이다.</b>
 * 0인 칸까지 색을 쓰면 "할 일 없음"과 "할 일 있음"이 같은 무게로 보여서
 * 운영자가 화면을 3초 만에 훑는 게 불가능해진다.
 *
 * <p>카드 바탕은 언제나 흰색이다. 상태는 <b>숫자와 아이콘 칩</b>이 지고, 위험할 때만
 * 테두리가 거든다. 배경을 통째로 물들이면 네 칸이 나란히 있을 때 화면이 얼룩덜룩해진다.
 *
 * <p>카드 전체가 링크다. 숫자를 보고 바로 그 큐로 들어가는 것이 이 화면의 전부다.
 */
export function QueueCard({ label, count, threshold, to, icon: Icon }: QueueCardProps) {
  const level: Level =
    count === 0 ? 'idle' : threshold && count >= threshold ? 'critical' : 'active';

  return (
    <Link
      to={to}
      className={cn(
        'group relative flex items-center gap-3.5 rounded-lg border bg-surface p-5 transition-all duration-150',
        'hover:border-border-strong hover:shadow-popover active:scale-[0.99]',
        level === 'critical' ? 'border-danger/35' : 'border-border-default',
      )}
    >
      <span
        className={cn(
          'flex size-9 shrink-0 items-center justify-center rounded-md',
          level === 'idle' && 'bg-subtle text-ink-muted',
          level === 'active' && 'bg-accent-subtle text-accent-ink',
          level === 'critical' && 'bg-danger-bg text-danger',
        )}
      >
        <Icon className="size-4" aria-hidden />
      </span>

      <span className="min-w-0 flex-1">
        <span className="block truncate text-xs font-medium text-ink-secondary">{label}</span>
        <span
          className={cn(
            'mt-1 block text-3xl font-bold leading-none tracking-[-0.02em]',
            level === 'idle' && 'text-ink-muted',
            level === 'active' && 'text-ink',
            level === 'critical' && 'text-danger',
          )}
        >
          {formatNumber(count)}
        </span>
      </span>

      <ArrowUpRight
        className="size-4 shrink-0 text-ink-muted opacity-0 transition-opacity group-hover:opacity-100"
        aria-hidden
      />
    </Link>
  );
}

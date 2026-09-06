import { Link } from 'react-router-dom';
import type { LucideIcon } from 'lucide-react';
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

/**
 * 처리 대기 큐 한 칸. 홈 화면 최상단에 네 개가 놓인다.
 *
 * <p>3단계로 나뉜다. <b>0이면 가라앉고, 1 이상이면 강조되고, 임계를 넘으면 위험이다.</b>
 * 0인 칸까지 색을 쓰면 "할 일 없음"과 "할 일 있음"이 같은 무게로 보여서
 * 운영자가 화면을 3초 만에 훑는 게 불가능해진다.
 *
 * <p>카드 전체가 링크다. 숫자를 보고 바로 그 큐로 들어가는 것이 이 화면의 전부다.
 */
export function QueueCard({ label, count, threshold, to, icon: Icon }: QueueCardProps) {
  const level = count === 0 ? 'idle' : threshold && count >= threshold ? 'critical' : 'active';

  return (
    <Link
      to={to}
      className={cn(
        'group flex items-center gap-3 rounded-lg border p-3 transition-colors',
        level === 'idle' && 'border-border-default bg-surface hover:bg-subtle',
        level === 'active' && 'border-accent bg-accent-subtle hover:brightness-95',
        level === 'critical' && 'border-danger bg-danger-bg hover:brightness-95',
      )}
    >
      <span
        className={cn(
          'flex size-8 shrink-0 items-center justify-center rounded',
          level === 'idle' && 'bg-subtle text-ink-muted',
          level === 'active' && 'bg-surface text-accent-ink',
          level === 'critical' && 'bg-surface text-danger',
        )}
      >
        <Icon className="size-4" aria-hidden />
      </span>

      <span className="min-w-0">
        <span className="block text-xs text-ink-secondary">{label}</span>
        <span
          className={cn(
            'block text-2xl font-semibold leading-tight',
            level === 'idle' && 'text-ink-muted',
            level === 'active' && 'text-accent-ink',
            level === 'critical' && 'text-danger',
          )}
        >
          {formatNumber(count)}
        </span>
      </span>
    </Link>
  );
}

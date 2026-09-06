import { ArrowDown, ArrowUp } from 'lucide-react';
import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';
import { formatNumber } from '@/lib/format';

type StatCardProps = {
  label: string;
  value: number | string;
  /** 값이 무엇을 세는지. 지표 이름만으로 모호한 것은 여기서 밝힌다. */
  hint?: string;
  /** 어제 대비 증감. 없으면 표시하지 않는다. 0을 "변화 없음"으로 꾸미지 않는다. */
  delta?: number;
  suffix?: ReactNode;
  className?: string;
};

export function StatCard({ label, value, hint, delta, suffix, className }: StatCardProps) {
  const rising = delta !== undefined && delta > 0;

  return (
    <div
      className={cn('rounded-lg border border-border-default bg-surface p-5', className)}
    >
      <p className="text-xs font-medium text-ink-secondary">{label}</p>

      <div className="mt-2 flex items-baseline gap-2">
        {/* 숫자는 라벨보다 두 단계 위에 둔다. 훑을 때 숫자만 눈에 들어와야 한다. */}
        <span className="text-3xl font-bold leading-none tracking-[-0.02em] text-ink">
          {typeof value === 'number' ? formatNumber(value) : value}
        </span>
        {suffix && <span className="text-xs text-ink-muted">{suffix}</span>}

        {delta !== undefined && delta !== 0 && (
          <span
            className={cn(
              'inline-flex items-center gap-0.5 text-xs font-medium',
              rising ? 'text-success' : 'text-ink-secondary',
            )}
          >
            {rising ? (
              <ArrowUp className="size-3" aria-hidden />
            ) : (
              <ArrowDown className="size-3" aria-hidden />
            )}
            {formatNumber(Math.abs(delta))}
            <span className="sr-only">어제 대비</span>
          </span>
        )}
      </div>

      {hint && <p className="mt-2 text-xs leading-snug text-ink-muted">{hint}</p>}
    </div>
  );
}

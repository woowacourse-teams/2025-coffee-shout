import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';
import { formatNumber } from '@/lib/format';

type StatCardProps = {
  label: string;
  value: number | string;
  /** 값이 무엇을 세는지. 지표 이름만으로 모호한 것은 여기서 밝힌다. */
  hint?: string;
  /** 어제 대비 증감. 값이 없으면 표시하지 않는다. 0을 "변화 없음"으로 꾸미지 않는다. */
  delta?: number;
  suffix?: ReactNode;
  className?: string;
};

export function StatCard({ label, value, hint, delta, suffix, className }: StatCardProps) {
  return (
    <div className={cn('rounded-lg border border-border-default bg-surface p-4', className)}>
      <p className="text-xs text-ink-secondary">{label}</p>

      <p className="mt-1 flex items-baseline gap-1.5">
        <span className="text-2xl font-semibold leading-none text-ink">
          {typeof value === 'number' ? formatNumber(value) : value}
        </span>
        {suffix && <span className="text-xs text-ink-muted">{suffix}</span>}
      </p>

      {delta !== undefined && delta !== 0 && (
        <p
          className={cn(
            'mt-1.5 text-xs font-medium',
            delta > 0 ? 'text-success' : 'text-ink-secondary',
          )}
        >
          {delta > 0 ? '+' : ''}
          {formatNumber(delta)} <span className="font-normal text-ink-muted">어제 대비</span>
        </p>
      )}

      {hint && <p className="mt-1.5 text-xs text-ink-muted">{hint}</p>}
    </div>
  );
}

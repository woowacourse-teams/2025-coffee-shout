import type { ComponentProps, ReactNode } from 'react';
import { cn } from '@/lib/cn';

/**
 * 카드. 그림자가 없다.
 *
 * 표가 많은 화면에서 그림자는 노이즈다. 층위는 1px 경계선과 배경 톤 차이로 만들고,
 * 그림자는 실제로 떠 있는 것(드롭다운, 다이얼로그)에만 쓴다.
 */
export function Card({ className, ...props }: ComponentProps<'div'>) {
  return (
    <div
      className={cn(
        'rounded-lg border border-border-default bg-surface',
        className,
      )}
      {...props}
    />
  );
}

type CardHeaderProps = {
  title: ReactNode;
  /** 제목 옆 보조 설명. 지표가 무엇을 세는지 한 줄로 밝힌다. */
  description?: ReactNode;
  actions?: ReactNode;
  className?: string;
};

export function CardHeader({ title, description, actions, className }: CardHeaderProps) {
  return (
    <div
      className={cn(
        'flex items-start justify-between gap-3 border-b border-border-default px-4 py-3',
        className,
      )}
    >
      <div className="min-w-0">
        <h2 className="text-base font-semibold text-ink">{title}</h2>
        {description && (
          <p className="mt-0.5 text-xs text-ink-muted">{description}</p>
        )}
      </div>
      {actions && <div className="flex shrink-0 items-center gap-2">{actions}</div>}
    </div>
  );
}

export function CardBody({ className, ...props }: ComponentProps<'div'>) {
  return <div className={cn('p-4', className)} {...props} />;
}

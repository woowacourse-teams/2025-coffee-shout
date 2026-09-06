import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

type PageHeaderProps = {
  title: string;
  /** 이 화면이 무엇을 위한 것인지 한 줄. 지표 화면이면 무엇을 세는지 밝힌다. */
  description?: ReactNode;
  actions?: ReactNode;
  className?: string;
};

/**
 * 모든 페이지의 첫 줄. 화면마다 제목 크기와 여백이 달라지면 화면을 옮길 때마다
 * 눈이 다시 자리를 잡아야 한다.
 */
export function PageHeader({ title, description, actions, className }: PageHeaderProps) {
  return (
    <header className={cn('flex items-start justify-between gap-4', className)}>
      <div className="min-w-0">
        <h2 className="text-xl font-semibold tracking-tight text-ink">{title}</h2>
        {description && (
          <p className="mt-1 max-w-2xl text-xs leading-relaxed text-ink-muted">{description}</p>
        )}
      </div>
      {actions && <div className="flex shrink-0 items-center gap-2">{actions}</div>}
    </header>
  );
}

/** 화면 안의 구획. 카드 여러 장을 묶을 때 쓴다. */
export function Section({
  title,
  description,
  actions,
  children,
  className,
}: {
  title?: string;
  description?: string;
  actions?: ReactNode;
  children: ReactNode;
  className?: string;
}) {
  return (
    <section className={cn('flex flex-col gap-2.5', className)}>
      {(title || actions) && (
        <div className="flex items-end justify-between gap-3">
          <div>
            {title && <h3 className="text-base font-semibold tracking-tight text-ink">{title}</h3>}
            {description && <p className="mt-0.5 text-xs text-ink-muted">{description}</p>}
          </div>
          {actions && <div className="flex shrink-0 items-center gap-2">{actions}</div>}
        </div>
      )}
      {children}
    </section>
  );
}

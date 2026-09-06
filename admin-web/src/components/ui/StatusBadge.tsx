import { cva, type VariantProps } from 'class-variance-authority';
import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

/**
 * 상태 배지. <b>색 + 점 + 텍스트 3중 인코딩</b>이다.
 *
 * 색만으로 구분하지 않는 이유는 두 가지다. 색각 이상 대응이고, 흑백 스크린샷을 슬랙에
 * 붙였을 때도 읽혀야 하기 때문이다. 운영 대화는 대개 스크린샷으로 시작한다.
 */
const badge = cva(
  'inline-flex items-center gap-1.5 rounded-sm px-1.5 py-0.5 text-2xs font-medium',
  {
    variants: {
      tone: {
        neutral: 'bg-subtle text-ink-secondary',
        success: 'bg-success-bg text-success',
        warning: 'bg-warning-bg text-warning',
        info: 'bg-info-bg text-info',
        danger: 'bg-danger-bg text-danger',
        accent: 'bg-accent-subtle text-accent-ink',
      },
    },
    defaultVariants: { tone: 'neutral' },
  },
);

const dot = cva('size-1.5 rounded-full', {
  variants: {
    tone: {
      neutral: 'bg-ink-muted',
      success: 'bg-success-dot',
      warning: 'bg-warning-dot',
      info: 'bg-info',
      danger: 'bg-danger',
      accent: 'bg-accent',
    },
  },
  defaultVariants: { tone: 'neutral' },
});

type StatusBadgeProps = VariantProps<typeof badge> & {
  children: ReactNode;
  className?: string;
};

export function StatusBadge({ tone, children, className }: StatusBadgeProps) {
  return (
    <span className={cn(badge({ tone }), className)}>
      <span className={dot({ tone })} aria-hidden />
      {children}
    </span>
  );
}

import { Slot } from '@radix-ui/react-slot';
import { cva, type VariantProps } from 'class-variance-authority';
import type { ComponentProps } from 'react';
import { cn } from '@/lib/cn';

/**
 * Primary 는 채움, danger 는 아웃라인이다.
 *
 * 접근성 때문이 아니라 밀도 때문이다. 표의 모든 행에 빨간 채움 버튼이 있으면 화면이
 * 경고로 뒤덮여 진짜 경고가 안 보인다. 채움 빨강은 확인 다이얼로그의 최종 실행 버튼
 * (`dangerSolid`)에서만 쓴다. 그 자리에는 primary 버튼이 없어 인접 혼동이 없다.
 */
const button = cva(
  'inline-flex items-center justify-center gap-1.5 whitespace-nowrap rounded font-medium ' +
    'transition-colors disabled:pointer-events-none disabled:opacity-50 ' +
    '[&_svg]:size-4 [&_svg]:shrink-0',
  {
    variants: {
      variant: {
        primary: 'bg-action text-ink-inverse hover:bg-action-hover',
        secondary:
          'bg-surface text-ink border border-border-strong hover:bg-subtle',
        ghost: 'text-ink-secondary hover:bg-subtle hover:text-ink',
        danger:
          'bg-surface text-danger border border-danger hover:bg-danger-bg',
        dangerSolid: 'bg-danger text-ink-inverse hover:brightness-110',
      },
      size: {
        sm: 'h-7 px-2.5 text-xs',
        md: 'h-8 px-3 text-sm',
        lg: 'h-9 px-4 text-base',
        icon: 'size-8',
      },
    },
    defaultVariants: { variant: 'secondary', size: 'md' },
  },
);

type ButtonProps = ComponentProps<'button'> &
  VariantProps<typeof button> & {
    /** `<Link>` 같은 다른 요소로 렌더링한다. */
    asChild?: boolean;
  };

export function Button({ className, variant, size, asChild, ...props }: ButtonProps) {
  const Component = asChild ? Slot : 'button';
  return <Component className={cn(button({ variant, size }), className)} {...props} />;
}

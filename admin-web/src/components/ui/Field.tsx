import { Search } from 'lucide-react';
import type { ComponentProps, ReactNode } from 'react';
import { cn } from '@/lib/cn';

const FIELD_BASE =
  'h-8 w-full rounded-md border border-border-strong bg-surface px-2.5 text-sm text-ink ' +
  'placeholder:text-ink-muted transition-colors ' +
  'hover:border-ink-muted focus:border-accent focus:outline-none ' +
  'disabled:cursor-not-allowed disabled:bg-subtle disabled:text-ink-muted';

export function Input({ className, ...props }: ComponentProps<'input'>) {
  return <input className={cn(FIELD_BASE, className)} {...props} />;
}

/**
 * 검색 입력. 돋보기를 안에 둔다.
 * 별도 "검색" 버튼을 두지 않는 이유는 목록 화면의 검색이 디바운스로 즉시 반영되기 때문이다.
 * 누를 필요가 없는 버튼이 놓여 있으면 눌러야 하는 줄 안다.
 */
export function SearchInput({ className, ...props }: ComponentProps<'input'>) {
  return (
    <div className={cn('relative', className)}>
      <Search
        className="pointer-events-none absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-ink-muted"
        aria-hidden
      />
      <input type="search" className={cn(FIELD_BASE, 'pl-8')} {...props} />
    </div>
  );
}

/**
 * 네이티브 select 를 스타일링만 한다. Radix 를 얹지 않는 이유는 백오피스의 선택지가
 * 대부분 열 개 미만이고, 네이티브가 키보드와 모바일에서 이미 잘 동작하기 때문이다.
 */
export function Select({ className, children, ...props }: ComponentProps<'select'>) {
  return (
    <select
      className={cn(
        FIELD_BASE,
        'cursor-pointer appearance-none bg-[length:14px] bg-[right_0.5rem_center] bg-no-repeat pr-7',
        "bg-[url(\"data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%236a7282' stroke-width='2'><path d='m6 9 6 6 6-6'/></svg>\")]",
        className,
      )}
      {...props}
    >
      {children}
    </select>
  );
}

type LabelProps = Omit<ComponentProps<'label'>, 'title'> & {
  /** 라벨 문구. 컨트롤은 children 으로 받는다. */
  text: ReactNode;
  hint?: ReactNode;
};

/**
 * 입력 한 줄. 라벨 문구, 컨트롤, 보조 문구 순으로 쌓는다.
 *
 * <p>문구와 컨트롤을 따로 받는다. 둘을 children 하나로 받으면 라벨 텍스트용 span 안에
 * input 이 들어가 문구 스타일이 컨트롤까지 덮고, 보조 문구가 컨트롤 아래가 아니라
 * 엉뚱한 자리에 붙는다.
 *
 * <p>{@code <label>} 로 감싸므로 {@code htmlFor} 없이도 클릭이 컨트롤로 간다.
 */
export function Label({ className, text, children, hint, ...props }: LabelProps) {
  return (
    <label className={cn('flex flex-col gap-1.5', className)} {...props}>
      <span className="text-xs font-medium text-ink-secondary">{text}</span>
      {children}
      {hint && <span className="text-2xs text-ink-muted">{hint}</span>}
    </label>
  );
}

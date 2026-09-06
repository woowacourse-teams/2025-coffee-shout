import { cn } from '@/lib/cn';

type Tab<T extends string> = { value: T; label: string };

type TabsProps<T extends string> = {
  tabs: Tab<T>[];
  value: T;
  onChange: (value: T) => void;
  className?: string;
};

/**
 * 세그먼트 컨트롤. 화면 안에서 같은 성격의 목록을 갈아 끼울 때 쓴다.
 *
 * <p>밑줄 탭이 아니라 알약 모양으로 둔다. 밑줄은 카드 머리에 놓으면 카드 경계선과
 * 겹쳐 어느 선이 탭인지 흐려진다. 알약은 카드 안 어디에 놓아도 자기 영역이 분명하다.
 *
 * <p>선택은 회색 채움으로만 표시한다. 여기에 코랄을 쓰면 "손이 필요하다"는 신호와
 * "지금 여기를 보고 있다"가 같은 색이 되어, 화면을 훑을 때 급한 것이 안 보인다.
 */
export function Tabs<T extends string>({ tabs, value, onChange, className }: TabsProps<T>) {
  return (
    <div
      role="tablist"
      className={cn(
        'inline-flex rounded-md border border-border-default bg-surface p-0.5',
        className,
      )}
    >
      {tabs.map((tab) => (
        <button
          key={tab.value}
          type="button"
          role="tab"
          aria-selected={value === tab.value}
          onClick={() => onChange(tab.value)}
          className={cn(
            'rounded-[0.3125rem] px-3 py-1 text-xs font-medium transition-colors',
            value === tab.value
              ? 'bg-subtle text-ink'
              : 'text-ink-muted hover:text-ink-secondary',
          )}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}

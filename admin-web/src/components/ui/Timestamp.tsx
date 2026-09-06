import { formatAbsolute, formatRelative } from '@/lib/format';
import { cn } from '@/lib/cn';

type TimestampProps = {
  value: string | Date | null | undefined;
  /** 상대시각을 숨긴다. 열 폭이 좁은 표에서 쓴다. */
  absoluteOnly?: boolean;
  className?: string;
};

/**
 * 절대시각과 상대시각을 함께 보여준다.
 *
 * 둘 다 필요하다. 상대시각만 있으면 "3일 전"이 정확히 언제인지 계산해야 하고,
 * 절대시각만 있으면 지금 급한 건인지 바로 안 보인다. 운영은 두 질문을 동시에 한다.
 */
export function Timestamp({ value, absoluteOnly, className }: TimestampProps) {
  if (!value) {
    return <span className={cn('text-ink-muted', className)}>-</span>;
  }

  const absolute = formatAbsolute(value);
  return (
    <span className={cn('whitespace-nowrap font-mono text-xs', className)}>
      <time dateTime={new Date(value).toISOString()}>{absolute}</time>
      {!absoluteOnly && (
        <span className="ml-1.5 text-ink-muted">({formatRelative(value)})</span>
      )}
    </span>
  );
}

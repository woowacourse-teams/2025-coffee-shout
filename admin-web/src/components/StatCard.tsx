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
      className={cn(
        // 네 칸이 나란히 설 때 높이가 같아야 한다. 보조 문구가 없는 칸만 짧아지면
        // 줄이 들쭉날쭉해 보인다. 문구 줄은 항상 자리를 차지하고 비어 있을 뿐이다.
        'flex h-full flex-col rounded-lg border border-border-default bg-surface px-4 py-3.5',
        className,
      )}
    >
      <p className="truncate text-xs font-medium text-ink-secondary" title={label}>
        {label}
      </p>

      <div className="mt-1.5 flex items-baseline gap-1.5">
        {/* 라벨보다 한 단계 위면 충분하다. 32px 까지 키우면 "1일 20시간" 같은 값이
         * 카드 폭을 넘어 줄바꿈되고, 그 순간 옆 카드와 높이가 어긋나 줄이 무너진다.
         * 숫자를 크게 하는 목적은 훑을 때 눈에 들어오게 하는 것이지 압도하는 게 아니다. */}
        <span className="whitespace-nowrap text-2xl font-bold leading-none tracking-[-0.02em] text-ink">
          {typeof value === 'number' ? formatNumber(value) : value}
        </span>
        {suffix && <span className="whitespace-nowrap text-xs text-ink-muted">{suffix}</span>}

        {/* 증감에 색을 붙이지 않는다. 방 생성이 준 것이 나쁜 일인지 좋은 일인지는
         * 지표마다 다르고, 초록이나 빨강을 달면 그 판단을 화면이 대신해 버린다.
         * 방향은 화살표가 말하고 판단은 사람이 한다. */}
        {delta !== undefined && delta !== 0 && (
          <span className="inline-flex items-center gap-0.5 text-xs font-medium text-ink-secondary">
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

      {/* 한 줄로 자른다. 두 줄이 되면 카드 높이가 제각각이 되고, 보조 문구 하나 때문에
       * 줄 전체가 흔들린다. 잘린 문구는 마우스를 올리면 전문이 뜬다. */}
      <p className="mt-auto truncate pt-2 text-xs text-ink-muted" title={hint}>
        {hint ?? ' '}
      </p>
    </div>
  );
}

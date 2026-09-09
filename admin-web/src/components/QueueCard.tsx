import { Link } from 'react-router-dom';
import { ArrowUpRight, type LucideIcon } from 'lucide-react';
import { cn } from '@/lib/cn';
import { formatNumber } from '@/lib/format';

type QueueCardProps = {
  label: string;
  count: number;
  to: string;
  icon: LucideIcon;
};

/**
 * 처리 대기 큐 한 칸. 홈 화면 최상단에 다섯 개가 놓인다.
 *
 * <p><b>0이냐 아니냐, 두 단계뿐이다.</b> 임계를 둬서 3단계로 나눠 봤지만, 색이 하나인
 * 시스템에서 세 번째 단계는 표현할 자리가 없다. 그리고 3건이든 42건이든 운영자가 할 일은
 * 같다. 가서 보는 것이다. 크기는 숫자가 이미 말한다.
 *
 * <p>0이면 통째로 물러난다. 0인 칸까지 색을 쓰면 "할 일 없음"과 "할 일 있음"이 같은
 * 무게로 보여서 화면을 3초 만에 훑는 게 불가능해진다.
 *
 * <h2>왜 두 줄로 쌓는가</h2>
 *
 * <p>한 줄에 라벨과 숫자를 나란히 두었을 때는 다섯 칸의 숫자가 제각각인 x 좌표에 서 있었다.
 * 라벨 길이가 칸마다 달라서다. 그러면 눈이 다섯 번 좌우로 움직여야 한다. 쌓아 두면 숫자가
 * <b>모두 같은 왼쪽 선</b>에서 시작해 한 번의 세로 훑기로 끝난다.
 *
 * <p>카드 바탕은 언제나 흰색이다. 배경을 통째로 물들이면 다섯 칸이 나란히 있을 때 화면이
 * 얼룩덜룩해진다. 색은 <b>아이콘 칩</b>이 진다. 평소 회색이던 칩이 로고색으로 채워지는
 * 것이 신호다. 채워진 칩의 글리프는 흰색이다. 아이콘 혼자 뜻을 지지 않고 옆에 라벨과
 * 숫자가 있어서, 흰 글리프여야 칩이 브랜드 마크처럼 하나로 읽힌다.
 *
 * <p>숫자는 잉크로 둔다. 로고색은 밝아서(흰 배경 2.78:1) 굵은 글씨로도 3:1 을 못 넘긴다.
 * 읽힐 만큼 어둡게 내리면 그건 로고색이 아니라 그냥 빨강이다. 색은 채움으로 쓰고 글자는
 * 잉크에 맡기면 둘 다 얻는다.
 */
export function QueueCard({ label, count, to, icon: Icon }: QueueCardProps) {
  const idle = count === 0;

  return (
    <Link
      to={to}
      className={cn(
        'group relative flex flex-col gap-3 rounded-lg border bg-surface px-4 py-3.5 shadow-card transition-all duration-150',
        'hover:-translate-y-px hover:border-border-strong hover:shadow-popover active:translate-y-0',
        idle ? 'border-border-default' : 'border-attention-mark/45',
      )}
    >
      <span className="flex items-center gap-2">
        <span
          className={cn(
            'flex size-7 shrink-0 items-center justify-center rounded-sm transition-colors',
            idle ? 'bg-subtle text-ink-muted' : 'bg-attention-solid text-attention-icon-on-solid',
          )}
        >
          <Icon className="size-4" strokeWidth={2.25} aria-hidden />
        </span>
        <span className="min-w-0 flex-1 truncate text-xs font-medium text-ink-secondary">
          {label}
        </span>
        <ArrowUpRight
          className="size-3.5 shrink-0 text-ink-muted opacity-0 transition-opacity group-hover:opacity-100"
          aria-hidden
        />
      </span>

      <span
        className={cn(
          'text-2xl font-bold leading-none tracking-metric',
          idle ? 'text-ink-muted' : 'text-ink',
        )}
      >
        {formatNumber(count)}
      </span>
    </Link>
  );
}

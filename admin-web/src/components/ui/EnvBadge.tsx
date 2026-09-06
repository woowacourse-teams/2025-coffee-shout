import { ENV_NAME, type EnvName } from '@/lib/env';
import { cn } from '@/lib/cn';

const TONE: Record<EnvName, string> = {
  PROD: 'bg-danger text-ink-inverse',
  DEV: 'bg-info text-ink-inverse',
  LOCAL: 'bg-subtle text-ink-secondary border border-border-strong',
};

/**
 * 지금 어느 환경을 보고 있는지. 상단 바에 <b>항상</b> 떠 있다.
 *
 * prod 에서 IP 차단 해제를 누르는 것과 dev 에서 누르는 것은 결과가 완전히 다르다.
 * 화면이 똑같이 생겼으므로 환경 표시가 유일한 구분 장치다. PROD 만 빨강인 것은
 * 눈에 먼저 걸리게 하려는 것이고, 그래서 danger 색을 상태가 아닌 곳에 쓰는 유일한 예외다.
 */
export function EnvBadge({ className }: { className?: string }) {
  return (
    <span
      className={cn(
        'rounded-sm px-1.5 py-0.5 text-2xs font-bold tracking-wide',
        TONE[ENV_NAME],
        className,
      )}
    >
      {ENV_NAME}
    </span>
  );
}

import { cn } from '@/lib/cn';
import { formatNumber, formatPercent } from '@/lib/format';

export type FunnelStage = {
  label: string;
  count: number;
};

type FunnelBarProps = {
  stages: FunnelStage[];
  className?: string;
};

/**
 * 방 진행 퍼널. 이 서비스가 잘 되고 있는지에 가장 직접적으로 답하는 그림이다.
 *
 * <p>막대 길이는 <b>1단계 대비 비율</b>이다. 앞 단계 대비로 그리면 매 단계가 100%에서
 * 시작해 어디서 크게 새는지가 안 보인다. 대신 단계마다 앞 단계 대비 전환율을 따로 적어
 * 두 질문("전체에서 얼마나 남았나", "여기서 얼마나 빠졌나")에 모두 답한다.
 *
 * <p>세로 막대가 아니라 가로다. 단계 이름이 한글이라 세로 축에 두면 잘리거나 회전한다.
 */
export function FunnelBar({ stages, className }: FunnelBarProps) {
  const first = stages[0]?.count ?? 0;

  return (
    <ol className={cn('flex flex-col gap-2', className)}>
      {stages.map((stage, index) => {
        const previous = index === 0 ? undefined : stages[index - 1]?.count;
        const widthRatio = first === 0 ? 0 : stage.count / first;
        const stepRatio =
          previous === undefined || previous === 0 ? undefined : stage.count / previous;
        const dropped = previous === undefined ? 0 : previous - stage.count;

        return (
          <li key={stage.label} className="grid grid-cols-[7rem_1fr_auto] items-center gap-3">
            <span className="truncate text-xs text-ink-secondary">{stage.label}</span>

            <span className="relative h-6 overflow-hidden rounded-sm bg-subtle">
              <span
                className="absolute inset-y-0 left-0 rounded-sm bg-chart-1 transition-[width]"
                style={{ width: `${Math.max(widthRatio * 100, stage.count > 0 ? 1.5 : 0)}%` }}
                aria-hidden
              />
              <span className="relative flex h-full items-center pl-2 text-xs font-medium text-ink">
                {formatNumber(stage.count)}
              </span>
            </span>

            <span className="w-28 text-right text-xs tabular-nums">
              {stepRatio === undefined ? (
                <span className="text-ink-muted">기준</span>
              ) : (
                <>
                  <span className="font-medium text-ink-secondary">
                    {formatPercent(stepRatio, 0)}
                  </span>
                  {dropped > 0 && (
                    <span className="ml-1 text-ink-muted">-{formatNumber(dropped)}</span>
                  )}
                </>
              )}
            </span>
          </li>
        );
      })}
    </ol>
  );
}

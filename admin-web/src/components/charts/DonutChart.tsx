import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import type { Bucket } from '@/api/types';
import { TOOLTIP_STYLE } from '@/components/charts/theme';
import { EmptyState } from '@/components/ui/EmptyState';
import { Swatch } from '@/components/ui/Legend';
import { formatNumber, formatPercent } from '@/lib/format';

type DonutChartProps = {
  slices: Bucket[];
  /**
   * 코랄로 칠할 조각의 이름.
   *
   * <p>도넛에서 색은 조각을 <b>구분</b>하는 데 쓰지 강조하는 데 쓰지 않는다. 다만 이 화면들은
   * 대개 한 조각을 보러 온다(완주한 방이 몇 %인가, 정상 통과가 몇 %인가). 그 하나만
   * 로고색으로 두고 나머지는 회색 농담으로 구분한다.
   */
  highlight?: string;
  /** 가운데에 적을 말. 총합 아래에 붙는다. */
  centerLabel: string;
  size?: number;
};

/**
 * 부분이 모여 전체를 이루는 구성비.
 *
 * <h2>언제 도넛이고 언제 막대인가</h2>
 *
 * <p>도넛은 <b>합이 하나의 전체</b>이고 항목이 대여섯 이하이며 순서가 없을 때만 쓴다.
 * 방이 어느 단계에서 멈췄나, 신고가 어느 갈래인가가 그렇다. 원 하나가 곧 "전체 방",
 * "전체 신고"라서 조각 크기가 바로 비율로 읽힌다.
 *
 * <p>순서가 있는 수치 구간(1명, 2~3명, 4~5명…)에는 쓰지 않는다. 원은 시작도 끝도 없어
 * 구간의 순서를 표현하지 못한다. 그건 히스토그램이 맡는다. 항목이 여덟을 넘고 꼴찌를
 * 봐야 하는 목록(게임별)도 아니다. 작은 조각끼리는 눈으로 비교가 안 된다.
 *
 * <p>가운데를 비워 총합을 적는다. 꽉 찬 원이면 그 자리가 놀고, 총합을 옆에 따로 적으면
 * 이 그림과 무관한 숫자처럼 보인다.
 *
 * <h2>회색 농담으로 조각을 나눈다</h2>
 *
 * <p>이 시스템의 색은 회색과 로고색 둘뿐이라 조각마다 다른 색을 줄 수 없다. 대신 진한
 * 회색에서 옅은 회색으로 내려가며 나누고, <b>이름과 값과 비율을 옆에 나란히 적는다.</b>
 * 조각을 색만으로 구분하게 두면 흑백 인쇄와 색각 이상에서 그림이 무의미해진다.
 */
export function DonutChart({ slices, highlight, centerLabel, size = 148 }: DonutChartProps) {
  const total = slices.reduce((sum, slice) => sum + slice.count, 0);

  if (total === 0) {
    return <EmptyState title={`${centerLabel} 기록이 없습니다`} />;
  }

  // 값이 0인 조각은 그리지 않는다. 그려도 보이지 않으면서 범례에 줄만 늘린다.
  const visible = slices.filter((slice) => slice.count > 0);
  // 조각 색은 회색 농담이다. 이 시스템의 색이 회색과 로고색 둘뿐이라 조각마다 다른 색을
  // 줄 수 없다. 이웃한 조각이 붙어 보이지 않게 진한 값과 옅은 값을 번갈아 둔다.
  const shades = [
    'var(--gray-500)',
    'var(--gray-300)',
    'var(--gray-600)',
    'var(--gray-400)',
    'var(--gray-200)',
  ];
  const colorOf = (label: string, index: number) =>
    label === highlight ? 'var(--accent)' : (shades[index % shades.length] ?? 'var(--gray-400)');

  return (
    <div className="flex items-center gap-5">
      <div className="relative shrink-0" style={{ width: size, height: size }}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={visible}
              dataKey="count"
              nameKey="label"
              innerRadius="62%"
              outerRadius="92%"
              paddingAngle={2}
              stroke="var(--bg-surface)"
              strokeWidth={1}
              // 12시에서 시계 방향. 기본값은 3시에서 반시계라 큰 조각이 아래에서 시작한다.
              startAngle={90}
              endAngle={-270}
              isAnimationActive={false}
            >
              {visible.map((slice, index) => (
                <Cell key={slice.label} fill={colorOf(slice.label, index)} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={TOOLTIP_STYLE}
              formatter={(value, name) => [
                `${formatNumber(Number(value ?? 0))} (${formatPercent(Number(value ?? 0) / total, 0)})`,
                String(name ?? ''),
              ]}
            />
          </PieChart>
        </ResponsiveContainer>

        {/* 차트 위에 겹치므로 마우스 이벤트를 통과시킨다. */}
        <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-xl font-bold leading-none tracking-metric text-ink">
            {formatNumber(total)}
          </span>
          <span className="mt-1 text-2xs text-ink-muted">{centerLabel}</span>
        </div>
      </div>

      {/* 범례가 곧 표다. 값을 오른쪽 끝에 맞춰 자릿수가 비교되게 한다. */}
      <ul className="flex min-w-0 flex-1 flex-col">
        {visible.map((slice, index) => (
          <li
            key={slice.label}
            className="flex items-center justify-between gap-3 border-b border-border-default py-1.5 last:border-b-0"
          >
            <span className="flex min-w-0 items-center gap-2">
              <Swatch color={colorOf(slice.label, index)} />
              <span className="min-w-0 truncate text-xs text-ink-secondary" title={slice.label}>
                {slice.label}
              </span>
            </span>
            <span className="flex shrink-0 items-baseline gap-1.5 tabular-nums">
              <span className="text-xs font-semibold text-ink">{formatNumber(slice.count)}</span>
              <span className="w-7 text-right text-2xs text-ink-muted">
                {formatPercent(slice.count / total, 0)}
              </span>
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}

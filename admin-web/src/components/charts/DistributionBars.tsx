import { Bar, BarChart, Cell, LabelList, ResponsiveContainer, Tooltip, XAxis } from 'recharts';
import type { Bucket } from '@/api/types';
import { TOOLTIP_STYLE } from '@/components/charts/theme';
import { EmptyState } from '@/components/ui/EmptyState';
import { formatNumber, formatPercent } from '@/lib/format';

type DistributionBarsProps = {
  data: Bucket[];
  /**
   * 숫자면 그 높이로 고정하고, {@code '100%'} 면 부모를 채운다.
   *
   * <p>카드 둘이 한 줄에 서면 격자가 낮은 쪽을 높은 쪽에 맞춰 늘리는데, 차트 높이가
   * 고정이면 늘어난 만큼이 카드 바닥의 빈 공간으로 남는다. 채우게 두면 그 자리를
   * 그림이 쓴다.
   */
  height?: number | string;
  /** 값이 전부 0일 때 칸 대신 보여줄 문구. */
  emptyTitle: string;
  emptyDescription?: string;
};

/**
 * 구간 분포. 인원수, 플레이 횟수, 소요 시간처럼 <b>순서가 있는 칸</b>을 그린다.
 *
 * <h2>왜 축이 없나</h2>
 *
 * <p>세로 눈금과 격자를 지우고 막대 위에 값을 직접 적는다. 칸이 다섯 개뿐이라 눈금을
 * 거쳐 값을 읽을 일이 없고, 격자가 있으면 카드 셋이 나란히 선 화면에서 가로선만 열다섯
 * 줄이 겹쳐 보인다. 정확한 값이 막대에 붙어 있으면 눈금은 하는 일이 없다.
 *
 * <h2>왜 1등만 색이 있나</h2>
 *
 * <p>분포에서 먼저 읽어야 하는 것은 <b>어느 칸이 가장 두꺼운가</b>다. 막대 다섯이 모두
 * 같은 회색이면 비슷한 두 칸의 높이를 눈으로 재게 된다. 1등만 로고색으로 채우고 나머지는
 * 회색으로 둔다. 게임별 비중 목록이 1위만 진하게 두는 것과 같은 규칙이다.
 *
 * <p>값이 같아 1등이 여럿이면 전부 칠한다. 임의로 하나를 고르면 새로고침할 때마다
 * 색이 옮겨 다닌다.
 */
export function DistributionBars({
  data,
  height = 168,
  emptyTitle,
  emptyDescription,
}: DistributionBarsProps) {
  const total = data.reduce((sum, bucket) => sum + bucket.count, 0);

  if (total === 0) {
    return <EmptyState title={emptyTitle} description={emptyDescription} />;
  }

  const top = Math.max(...data.map((bucket) => bucket.count));

  // 바닥 높이를 둔다. height="100%" 는 부모 높이가 안 잡히면 0이 되고, 그때 차트가
  // 오류도 빈 상태도 아닌 <b>아무것도 없는 카드</b>로 남는다. 실제로 카드 하나에
  // flex 지정을 빠뜨려 그 일이 있었다.
  return (
    <div className="h-full min-h-36 w-full">
      <ResponsiveContainer width="100%" height={height}>
        {/* 위쪽에 여백을 둔다. 막대 위의 숫자가 카드 안쪽 경계에 닿으면 잘린다. */}
        <BarChart data={data} margin={{ top: 18, right: 4, bottom: 0, left: 4 }}>
          <XAxis
            dataKey="label"
            tick={{ fontSize: 11, fill: 'var(--chart-axis)' }}
            tickLine={false}
            axisLine={false}
            interval={0}
          />
          <Tooltip
            cursor={{ fill: 'var(--chart-grid)', fillOpacity: 0.55 }}
            contentStyle={TOOLTIP_STYLE}
            formatter={(value) => [
              `${formatNumber(Number(value ?? 0))} (${formatPercent(Number(value ?? 0) / total, 0)})`,
              '',
            ]}
            // 이름이 없는 한 계열이라 범례 줄을 지운다. 두면 빈 색 사각형만 남는다.
            labelFormatter={(label: string) => label}
          />
          <Bar dataKey="count" maxBarSize={44} radius={[3, 3, 0, 0]} isAnimationActive={false}>
            {data.map((bucket) => (
              <Cell
                key={bucket.label}
                fill={bucket.count === top ? 'var(--chart-1)' : 'var(--gray-300)'}
              />
            ))}
            <LabelList
              dataKey="count"
              position="top"
              offset={6}
              fontSize={11}
              fill="var(--chart-axis)"
              // 0인 칸에는 숫자를 적지 않는다. 바닥에 0이 줄줄이 서면 그 줄이 축처럼 보인다.
              formatter={(value: number) => (value === 0 ? '' : formatNumber(value))}
            />
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

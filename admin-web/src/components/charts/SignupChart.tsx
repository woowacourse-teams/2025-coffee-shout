import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { AXIS_STYLE, GRID_STYLE, TOOLTIP_STYLE } from '@/components/charts/theme';
import { formatNumber } from '@/lib/format';

type SignupChartProps = {
  data: { date: string; count: number }[];
  /**
   * 숫자면 그 높이로 고정하고, {@code '100%'} 면 부모를 채운다.
   *
   * <p>카드 둘이 한 줄에 서면 격자가 낮은 쪽을 높은 쪽에 맞춰 늘리는데, 차트 높이가
   * 고정이면 늘어난 만큼이 카드 바닥의 빈 공간으로 남는다. 채우게 두면 그 자리를
   * 그림이 쓴다.
   */
  height?: number | string;
};

/**
 * 일자별 가입.
 *
 * <p>선이 아니라 막대다. 가입은 "그날 몇 명"인 이산 합계라 이어 그릴 양이 아니고, 하루
 * 0명인 날이 흔한데 선으로 그리면 그 날이 두 점 사이의 골짜기로 뭉개진다. 막대는 0인 날을
 * 빈칸으로 정직하게 남긴다.
 *
 * <p>서버가 가입 없는 날도 0으로 채워 보낸다. 화면에서 달력을 만들면 시간대 판단이
 * 서버와 화면 두 곳에 생긴다.
 */
export function SignupChart({ data, height = 180 }: SignupChartProps) {
  // 바닥 높이를 둔다. height="100%" 는 부모 높이가 안 잡히면 0이 되고, 그때 차트가
  // 오류도 빈 상태도 아닌 <b>아무것도 없는 카드</b>로 남는다. 실제로 카드 하나에
  // flex 지정을 빠뜨려 그 일이 있었다.
  return (
    <div className="h-full min-h-36 w-full">
      <ResponsiveContainer width="100%" height={height}>
        <BarChart data={data} margin={{ top: 4, right: 4, bottom: 0, left: 0 }}>
          <CartesianGrid {...GRID_STYLE} />
          <XAxis
            dataKey="date"
            tickFormatter={(value: string) => value.slice(5).replace('-', '/')}
            {...AXIS_STYLE}
            minTickGap={16}
          />
          <YAxis {...AXIS_STYLE} width={32} allowDecimals={false} tickCount={4} />
          <Tooltip
            cursor={{ fill: 'var(--chart-grid)', fillOpacity: 0.55 }}
            contentStyle={TOOLTIP_STYLE}
            formatter={(value) => [`${formatNumber(Number(value ?? 0))}명`, '가입']}
          />
          <Bar
            dataKey="count"
            name="가입"
            fill="var(--chart-1)"
            maxBarSize={14}
            radius={[3, 3, 0, 0]}
            isAnimationActive={false}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

import {
  Bar,
  CartesianGrid,
  ComposedChart,
  Line,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { DailyTrend } from '@/api/types';
import { ACTIVE_DOT, AXIS_STYLE, GRID_STYLE, TOOLTIP_STYLE } from '@/components/charts/theme';
import { formatNumber } from '@/lib/format';

type TrendChartProps = {
  data: DailyTrend[];
  height?: number;
};

/**
 * 최근 흐름. 오늘 숫자만으로는 "0인데 정상인가"에 답할 수 없다.
 *
 * <h2>막대와 선을 섞는 이유</h2>
 *
 * <p>세 값의 성격이 다르다. 방 생성과 완주는 <b>하루에 몇 건</b>인 이산 합계라 막대가 맞다.
 * 예전에는 면적으로 그렸는데, 면적은 날짜 사이를 이어 붙여 연속량처럼 보이게 한다.
 * 어제와 오늘 사이에 중간값이 있는 것이 아니다.
 *
 * <p>둘을 나란히 세우면 <b>완주가 생성을 얼마나 따라갔는지</b>가 막대 높이 차이로 바로
 * 읽힌다. 면적 위에 선을 겹쳐 두었을 때는 둘이 같은 방향으로 움직여서 겹친 덩어리만
 * 보이고 정작 그 차이가 안 보였다.
 *
 * <p>참여자는 "몇 명이 있었나"라 성격이 다르고 자릿수도 한 단계 크다. 선으로 두면 막대와
 * 섞이지 않으면서 같은 눈금 위에서 위아래 관계가 읽힌다.
 *
 * <h2>축은 하나다</h2>
 *
 * <p>참여자가 방보다 훨씬 커서 이중 축이 당기지만 쓰지 않는다. 축을 둘로 나누면 두 계열의
 * 교차점이 아무 의미가 없어지고, 축 배율만 바꿔서 원하는 그림을 만들 수 있게 된다.
 * 참여자는 언제나 방보다 크므로 한 축에서도 관계가 무너지지 않는다.
 */
export function TrendChart({ data, height = 220 }: TrendChartProps) {
  return (
    <ResponsiveContainer width="100%" height={height}>
      <ComposedChart data={data} margin={{ top: 4, right: 4, bottom: 0, left: -18 }} barGap={2}>
        <CartesianGrid {...GRID_STYLE} />
        <XAxis
          dataKey="date"
          tickFormatter={(value: string) => value.slice(5).replace('-', '/')}
          {...AXIS_STYLE}
          minTickGap={16}
        />
        <YAxis
          {...AXIS_STYLE}
          width={44}
          allowDecimals={false}
        />
        <Tooltip
          // 그날 칸 전체를 옅게 누른다. 선 차트의 세로 실선을 그대로 쓰면 막대 사이 어디를
          // 가리키는지 애매해진다.
          cursor={{ fill: 'var(--chart-grid)', fillOpacity: 0.55 }}
          contentStyle={TOOLTIP_STYLE}
          labelFormatter={(value) => String(value)}
          formatter={(value, name) => [formatNumber(Number(value ?? 0)), String(name ?? '')]}
        />

        {/* 90일까지 열려 있어 막대가 얇아진다. 최대 폭을 잡아 두면 구간이 짧을 때 막대
         * 하나가 통짜로 뚱뚱해지는 것을 막고, 길어지면 알아서 얇아진다. */}
        <Bar
          dataKey="created"
          name="방 생성"
          fill="var(--chart-1)"
          maxBarSize={14}
          radius={[3, 3, 0, 0]}
        />
        <Bar
          dataKey="completed"
          name="완주"
          fill="var(--chart-2)"
          maxBarSize={14}
          radius={[3, 3, 0, 0]}
        />
        <Line
          type="monotone"
          dataKey="players"
          name="참여자"
          stroke="var(--chart-3)"
          strokeWidth={2}
          dot={false}
          activeDot={ACTIVE_DOT}
        />
      </ComposedChart>
    </ResponsiveContainer>
  );
}

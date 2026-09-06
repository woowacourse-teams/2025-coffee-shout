import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { DailyTrend } from '@/api/types';
import { formatNumber } from '@/lib/format';

type TrendChartProps = {
  data: DailyTrend[];
  height?: number;
};

/**
 * 최근 흐름. 오늘 숫자만으로는 "0인데 정상인가"에 답할 수 없다.
 *
 * <p>축을 하나만 쓴다. 방 생성과 참여자는 자릿수가 다르지만 이중 축을 쓰면 두 선의
 * 교차점이 아무 의미가 없어지고, 축 배율을 바꿔 원하는 그림을 만들 수 있게 된다.
 * 참여자는 방보다 항상 크므로 한 축에서도 위아래 관계가 읽힌다.
 *
 * <p>격자와 축은 뒤로 물린다. 읽어야 하는 것은 선의 모양이지 눈금이 아니다.
 */
export function TrendChart({ data, height = 220 }: TrendChartProps) {
  return (
    <ResponsiveContainer width="100%" height={height}>
      <AreaChart data={data} margin={{ top: 4, right: 4, bottom: 0, left: -18 }}>
        <defs>
          <linearGradient id="trend-created" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="var(--chart-1)" stopOpacity={0.22} />
            <stop offset="100%" stopColor="var(--chart-1)" stopOpacity={0} />
          </linearGradient>
        </defs>

        <CartesianGrid stroke="var(--chart-grid)" vertical={false} />
        <XAxis
          dataKey="date"
          tickFormatter={(value: string) => value.slice(5).replace('-', '/')}
          tick={{ fontSize: 11, fill: 'var(--chart-axis)' }}
          tickLine={false}
          axisLine={false}
          minTickGap={16}
        />
        <YAxis
          tick={{ fontSize: 11, fill: 'var(--chart-axis)' }}
          tickLine={false}
          axisLine={false}
          width={44}
          allowDecimals={false}
        />
        <Tooltip
          cursor={{ stroke: 'var(--border-strong)' }}
          contentStyle={{
            borderRadius: 'var(--radius)',
            border: '1px solid var(--border)',
            fontSize: '12px',
            boxShadow: 'var(--shadow-popover)',
          }}
          labelFormatter={(value) => String(value)}
          formatter={(value, name) => [formatNumber(Number(value ?? 0)), String(name ?? '')]}
        />

        <Area
          type="monotone"
          dataKey="created"
          name="방 생성"
          stroke="var(--chart-1)"
          strokeWidth={2.5}
          fill="url(#trend-created)"
          dot={false}
        />
        <Area
          type="monotone"
          dataKey="completed"
          name="완주"
          stroke="var(--chart-2)"
          strokeWidth={2}
          fill="none"
          dot={false}
        />
        <Area
          type="monotone"
          dataKey="players"
          name="참여자"
          stroke="var(--chart-3)"
          strokeWidth={2}
          strokeDasharray="4 3"
          fill="none"
          dot={false}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
}


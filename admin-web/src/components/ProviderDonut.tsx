import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import type { ProviderStats } from '@/api/types';
import { TOOLTIP_STYLE } from '@/components/charts/theme';
import { Swatch } from '@/components/ui/Legend';
import { formatNumber, formatPercent } from '@/lib/format';

/**
 * 제공자 색은 각 브랜드가 실제로 쓰는 색이다.
 *
 * <p>여기만 회색과 로고색 둘뿐인 규칙에서 벗어난다. 이유는 이 셋이 <b>우리가 이름 붙인
 * 항목이 아니라 밖에서 온 고유명사</b>이기 때문이다. 노란색 조각이 카카오라는 것은 설명이
 * 필요 없다. 로고색 농담으로 셋을 나누면 범례를 매번 다시 봐야 한다.
 *
 * <p>구글은 회색이다. 구글의 파랑을 쓰면 우리 로고색과 부딪히면서 화면에서 가장 먼저
 * 눈에 띄는 조각이 되는데, 제공자 사이에 그런 우선순위는 없다.
 *
 * <h2>카카오 노랑은 흰 배경 대비 1.28:1 이다</h2>
 *
 * <p>마크 기준 3:1 에 한참 못 미친다. 3:1 을 맞추려면 {@code #b58f00} 까지 내려야 하는데
 * 그건 카카오 노랑이 아니라 올리브색이고, 브랜드로 알아보게 하려고 브랜드 색을 쓰는
 * 의미가 사라진다.
 *
 * <p>그래서 색을 바꾸는 대신 <b>조각마다 1px 테두리</b>를 둘렀다. 노란 조각이 흰 바탕에
 * 녹아 빈 공간처럼 보이는 것을 테두리가 막는다. 그리고 이 그림에서 <b>어느 조각이 무엇인지는
 * 색이 혼자 지지 않는다</b>. 옆의 범례에 이름과 건수와 비율이 나란히 적혀 있고 툴팁도
 * 이름을 읽어 준다. {@code --chart-1}(로고색 선)에 적용한 것과 같은 예외다.
 */
const PROVIDER: Record<string, { label: string; color: string }> = {
  google: { label: '구글', color: 'var(--provider-google)' },
  kakao: { label: '카카오', color: 'var(--provider-kakao)' },
  naver: { label: '네이버', color: 'var(--provider-naver)' },
};

/**
 * 소셜 제공자 분포.
 *
 * <h2>왜 여기는 도넛인가</h2>
 *
 * <p>게임별 비중은 막대로 그렸는데 여기는 도넛이다. 같은 "비중"이라도 묻는 것이 다르다.
 * 게임은 여덟 개가 넘고 <b>꼴찌가 얼마나 안 쓰이는지</b>를 봐야 해서 작은 값끼리 비교가
 * 되어야 한다. 원그래프는 그걸 못 한다.
 *
 * <p>제공자는 셋이고, 묻는 것은 <b>"카카오가 절반을 넘나"</b>처럼 전체 대비 크기다.
 * 조각 셋의 부채꼴 크기는 눈으로 바로 비교되고, 합이 하나의 원을 이룬다는 사실 자체가
 * 정보다. 항목이 넷을 넘어가면 이 판단을 다시 해야 한다.
 *
 * <p>가운데를 비워 총합을 적는다. 꽉 찬 원이면 그 자리가 놀고, 총합을 옆에 따로 적으면
 * 이 그림과 무관한 숫자처럼 보인다.
 *
 * <p><b>합이 회원 수가 아니다.</b> 한 사람이 여러 소셜을 연결할 수 있어서, 이 원은
 * "회원의 구성"이 아니라 "연결의 구성"이다. 그 차이를 화면에 적어 둔다.
 */
export function ProviderDonut({ stats, height = 200 }: { stats: ProviderStats; height?: number }) {
  const slices = stats.providers.map((row) => ({
    key: row.provider,
    name: PROVIDER[row.provider]?.label ?? row.provider,
    color: PROVIDER[row.provider]?.color ?? 'var(--chart-1)',
    value: row.count,
  }));
  const total = slices.reduce((sum, slice) => sum + slice.value, 0);

  return (
    <div className="flex flex-col gap-4">
      {/* 도넛 위, 범례 아래로 쌓는다.
        *
        * 한때 좌우로 놓고 범례를 가로 세 칸으로 깔았다. 그때는 이 카드가 본문 폭 전체를
        * 쓰는 자리였다. 홈으로 옮기면서 3분의 1 열에 들어가자 한 칸이 100px 이 되어
        * 제공자 이름이 통째로 잘리고 숫자와 비율만 서로 붙어 남았다. 좁은 자리에서는
        * 세로가 맞다. */}
      <div className="flex flex-col items-center gap-4">
      <div className="relative shrink-0" style={{ width: height, height }}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={slices}
              dataKey="value"
              nameKey="name"
              innerRadius="62%"
              outerRadius="92%"
              // 조각 사이를 벌리고 테두리를 두른다. 벌림만으로는 밝은 조각(카카오)이
              // 흰 바탕에 녹고, 테두리만으로는 어두운 두 조각이 한 덩어리로 보인다.
              paddingAngle={2}
              stroke="var(--border-strong)"
              strokeWidth={1}
              // 12시에서 시계 방향. 기본값은 3시에서 반시계라 큰 조각이 아래에서 시작한다.
              startAngle={90}
              endAngle={-270}
              isAnimationActive={false}
            >
              {slices.map((slice) => (
                <Cell key={slice.key} fill={slice.color} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={TOOLTIP_STYLE}
              formatter={(value, name) => [
                `${formatNumber(Number(value ?? 0))}건 (${
                  total === 0 ? '-' : formatPercent(Number(value ?? 0) / total, 0)
                })`,
                String(name ?? ''),
              ]}
            />
          </PieChart>
        </ResponsiveContainer>

        {/* 도넛 가운데. 차트 위에 겹치므로 마우스 이벤트를 통과시킨다. */}
        <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-xl font-bold leading-none tracking-metric text-ink">
            {formatNumber(total)}
          </span>
          <span className="mt-1 text-2xs text-ink-muted">연결</span>
        </div>
      </div>

      {/* 범례가 곧 표다. 색 옆에 이름과 값과 비율을 함께 적어 두면 조각에 마우스를
        * 올리지 않고도 읽힌다. 조각에 직접 라벨을 붙이면 작은 조각에서 글자가 겹친다.
        *
        * 한 줄에 하나씩 쌓고 값을 오른쪽 끝에 맞춘다. 세 값이 같은 세로선에 서야 자릿수가
        * 비교되고, 그 비교 방향이 도넛 조각의 크기 비교와 어긋나지 않는다. */}
      <div className="flex w-full min-w-0 flex-col">
        {slices.map((slice) => (
          <div
            key={slice.key}
            className="flex items-center justify-between gap-3 border-b border-border-default py-2 last:border-b-0"
          >
            <span className="flex min-w-0 items-center gap-2">
              {/* 조각과 같은 이유로 테두리를 둔다. 카카오 노랑 사각형은 테두리가 없으면
                * 흰 바탕에서 아예 안 보인다. */}
              <Swatch color={slice.color} bordered />
              <span className="min-w-0 truncate text-xs font-medium text-ink-secondary">
                {slice.name}
              </span>
            </span>
            <span className="flex shrink-0 items-baseline gap-1.5 tabular-nums">
              <span className="text-xl font-bold leading-none tracking-metric text-ink">
                {formatNumber(slice.value)}
              </span>
              <span className="text-xs text-ink-muted">
                {total === 0 ? '-' : formatPercent(slice.value / total, 0)}
              </span>
            </span>
          </div>
        ))}
      </div>
      </div>

      {/* 단서는 범례 안이 아니라 카드 바닥에 한 줄로 둔다. 범례 목록의 마지막 항목처럼
        * 놓여 있으면 네 번째 제공자로 잘못 읽힌다. */}
      <p className="text-2xs leading-relaxed text-ink-muted">
        연결 {formatNumber(total)}건, 회원 {formatNumber(stats.userCount)}명. 한 사람이 여러
        소셜을 연결할 수 있어 두 숫자는 맞지 않습니다.
      </p>
    </div>
  );
}

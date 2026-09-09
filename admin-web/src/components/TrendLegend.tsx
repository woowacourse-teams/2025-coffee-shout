/**
 * 추이 차트 범례.
 *
 * <p>차트와 <b>다른 파일</b>에 둔다. 같은 모듈에 있으면 이 범례를 정적으로 가져오는
 * 순간 recharts 까지 딸려 와서, 차트를 지연 로드해도 번들이 쪼개지지 않는다.
 *
 * <p>계열 셋을 색만으로 구분하지 않고 이름을 붙인다. 표식 모양도 차트와 맞춘다.
 * 막대인 것은 네모, 선인 것은 선이다. 셋 다 같은 점으로 그리면 범례를 보고 나서
 * 차트에서 어느 것이 막대이고 어느 것이 선인지 다시 찾아야 한다.
 */
export function TrendLegend() {
  return (
    <div className="flex flex-wrap items-center gap-3 text-xs text-ink-secondary">
      <LegendItem color="var(--chart-1)" label="방 생성" shape="bar" />
      <LegendItem color="var(--chart-2)" label="완주" shape="bar" />
      <LegendItem color="var(--chart-3)" label="참여자" shape="line" />
    </div>
  );
}

function LegendItem({
  color,
  label,
  shape,
}: {
  color: string;
  label: string;
  shape: 'bar' | 'line';
}) {
  return (
    <span className="inline-flex items-center gap-1.5">
      {shape === 'bar' ? (
        <span className="h-3 w-2 rounded-[2px]" style={{ backgroundColor: color }} aria-hidden />
      ) : (
        <span className="h-0.5 w-4 rounded-full" style={{ backgroundColor: color }} aria-hidden />
      )}
      {label}
    </span>
  );
}

/**
 * 추이 차트 범례.
 *
 * <p>차트와 <b>다른 파일</b>에 둔다. 같은 모듈에 있으면 이 범례를 정적으로 가져오는
 * 순간 recharts 까지 딸려 와서, 차트를 지연 로드해도 번들이 쪼개지지 않는다.
 *
 * <p>선 셋을 색만으로 구분하지 않고 이름을 붙인다.
 */
export function TrendLegend() {
  return (
    <div className="flex flex-wrap items-center gap-3 text-xs text-ink-secondary">
      <LegendItem color="var(--chart-1)" label="방 생성" />
      <LegendItem color="var(--chart-2)" label="완주" />
      <LegendItem color="var(--chart-3)" label="참여자" dashed />
    </div>
  );
}

function LegendItem({
  color,
  label,
  dashed,
}: {
  color: string;
  label: string;
  dashed?: boolean;
}) {
  return (
    <span className="inline-flex items-center gap-1.5">
      <span
        className="h-0.5 w-4 rounded-full"
        style={
          dashed
            ? {
                backgroundImage: `repeating-linear-gradient(90deg, ${color} 0 4px, transparent 4px 7px)`,
              }
            : { backgroundColor: color }
        }
        aria-hidden
      />
      {label}
    </span>
  );
}

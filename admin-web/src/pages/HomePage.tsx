import { Hourglass, MessageSquareWarning, ServerCog, ShieldBan, SpellCheck } from 'lucide-react';
import { Link } from 'react-router-dom';
import {
  useActionQueue,
  useAuditLogs,
  useDailySummary,
  useNicknameAuditQuality,
  useReportSla,
  useTrend,
} from '@/api/queries';
import { lazy, Suspense } from 'react';
import type { DailyTrend } from '@/api/types';
import { ActivityFeed } from '@/components/ActivityFeed';
import { FunnelBar } from '@/components/FunnelBar';
import { ActionQueueStrip } from '@/components/ActionQueueStrip';
import { TrendLegend } from '@/components/TrendLegend';
import { Button } from '@/components/ui/Button';
import { Card, CardBody, CardHeader } from '@/components/ui/Card';
import { Skeleton } from '@/components/ui/EmptyState';
import { Loaded } from '@/components/ui/Loaded';
import { TileGrid, TileSkeletons } from '@/components/ui/TileGrid';
import { MetricRow } from '@/components/ui/MetricRow';
import { PageHeader, Section } from '@/components/ui/PageHeader';
import { formatDurationMinutes, formatPercent } from '@/lib/format';

/**
 * recharts 는 이 차트 하나에만 쓰이는데 gzip 108KB 를 더한다. 지연 로드하면 로그인 화면과
 * 목록 화면이 그 무게를 지지 않는다. 홈은 차트가 조금 늦게 떠도 나머지가 먼저 보인다.
 *
 * <p>범례를 {@link TrendLegend} 로 떼어낸 것이 이 분리의 전제다. 같은 모듈에서 범례를
 * 정적으로 가져오면 recharts 가 메인 청크로 따라 들어와 지연 로드가 무효가 된다.
 *
 * <p>{@code Sparkline} 은 반대로 정적으로 가져온다. recharts 를 쓰지 않고 {@code polyline}
 * 하나로 그리기 때문에 무게가 없다.
 */
/**
 * 이 화면의 유일한 좌우 분할이다. 넓은 쪽 2, 좁은 쪽 1.
 *
 * <p>한때 줄마다 비율이 달랐다. 추이와 오늘은 2:1, 퍼널과 조치는 3:2 였다. 각 줄만 보면
 * 그럴듯했지만 <b>위아래로 놓고 보면 오른쪽 카드들의 왼쪽 모서리가 어긋났다.</b> 눈에 딱
 * 짚이지는 않는데 화면 전체가 어수선해 보이는 원인이 이런 것이다.
 *
 * <p>줄마다 최적 비율을 따로 찾지 않는다. 하나로 고정하고 내용을 거기 맞춘다.
 */
const SPLIT = 'grid gap-4 xl:grid-cols-[minmax(0,2fr)_minmax(18rem,1fr)]';

const TrendChart = lazy(() =>
  import('@/components/TrendChart').then((module) => ({ default: module.TrendChart })),
);

/**
 * 홈. 운영자가 로그인해서 <b>3초 안에</b> 세 가지에 답할 수 있어야 한다.
 * "지금 처리할 일이 있나", "서비스가 평소만큼 돌고 있나", "누가 방금 뭘 바꿨나".
 *
 * <p>담는 지표의 기준은 하나다. <b>Grafana 가 못 보는 것.</b> 응답시간, 에러율, JVM 은
 * 그쪽이 이미 본다. 두 곳이 다른 숫자를 말하는 순간 양쪽 다 신뢰를 잃는다.
 * 여기 있는 것은 전부 도메인 조인이거나 우리 DB 에만 있는 기록이다.
 *
 * <h2>배치는 위 세 질문의 순서를 따른다</h2>
 *
 * <p>① 처리 대기와 운영 품질을 위에 붙여 둔다. "밀렸나"와 "잘 처리하고 있나"는 같은
 * 질문의 앞뒤인데, 운영 품질이 화면 맨 아래에 있어서 둘을 함께 보려면 스크롤해야 했다.
 *
 * <p>② 그다음이 서비스 흐름이다. 14일 추이와 오늘 숫자를 나란히 두고, 그 아래 오늘 퍼널을
 * 놓는다. 셋 다 "지금 정상인가"에 답한다.
 *
 * <p>③ 최근 조치가 맨 아래에 가로로 눕는다. 시간 순 기록이라 세로로 길고, 옆에 무엇을
 * 두든 높이가 안 맞았다.
 *
 * <p><b>게임별 플레이는 뺐다.</b> 30일 집계라 이 화면의 "지금"과 시간축이 다르고,
 * 서비스 분석 화면에 똑같은 카드가 이미 있다. 같은 카드를 두 곳에 두면 한쪽만 고치는
 * 날이 온다.
 */
export function HomePage() {
  const queue = useActionQueue();
  const summary = useDailySummary();
  const trend = useTrend(14);
  const sla = useReportSla(30);
  const auditQuality = useNicknameAuditQuality(30);
  // 다섯 건이다. 옆 퍼널 카드와 높이를 맞추려고 정한 수다. 더 보려면 전체 보기로 간다.
  const logs = useAuditLogs(5);

  const series = trend.data ?? [];

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title="홈"
        description="처리할 일과 서비스 흐름. 인프라 지표는 Grafana(status.zzol.site)가 봅니다."
      />

      <Section title="처리 대기" description="숫자를 누르면 해당 화면으로 갑니다.">
        <Loaded query={queue} skeleton={<TileGrid columns={5}><TileSkeletons count={5} height="h-[4.75rem]" /></TileGrid>}>
          {(data) => (
            <ActionQueueStrip
              items={[
                // 맨 앞이다. 나머지 넷은 사람이 판단해 줄 일이고 쌓이는 게 정상이지만,
                // 이건 시스템이 멈춘 것이라 0이 아닌 순간 다른 무엇보다 먼저 봐야 한다.
                {
                  label: '격리 메시지',
                  count: data.deadLetters,
                  to: '/ops',
                  icon: ServerCog,
                  critical: true,
                },
                {
                  label: '미처리 신고',
                  count: data.pendingReports,
                  to: '/reports',
                  icon: MessageSquareWarning,
                },
                // 화면에 FLAGGED, PENDING 을 그대로 적던 것을 걷어냈다. 서버 enum 이름이고
                // 운영자가 쓰는 말이 아니다. 검열 화면이 이미 각 상태에 붙여 둔 설명을
                // 그대로 가져왔다.
                {
                  label: 'AI가 걸러낸 닉네임',
                  count: data.flaggedNicknames,
                  to: '/profanity',
                  icon: SpellCheck,
                },
                {
                  label: 'AI가 판단 못한 닉네임',
                  count: data.pendingNicknames,
                  to: '/profanity',
                  icon: Hourglass,
                },
                { label: '차단 IP', count: data.blockedIps, to: '/ip-blocks', icon: ShieldBan },
              ]}
            />
          )}
        </Loaded>
      </Section>

      {/* 처리 대기 바로 아래다. 밀린 양과 처리 품질은 한 덩어리로 봐야 판단이 선다.
        *
        * 타일 넷을 늘어놓다가 두 묶음으로 접었다. 네 지표가 같은 무게로 나란히 서 있으면
        * 서로 무관한 넷으로 읽히는데, 실제로는 <b>질문이 둘</b>이다. 신고를 얼마나 빨리
        * 처리하나, AI 검열이 얼마나 정확한가. 각 묶음 안에서만 값을 견주면 되고 묶음끼리는
        * 견줄 일이 없다.
        *
        * 접으면서 "검열 오탐 / 미탐 = 0 / 0" 도 풀렸다. 라벨의 슬래시와 값의 슬래시가
        * 겹쳐서 어느 쪽이 오탐인지 세어 봐야 했는데, 줄로 나누면 각자 이름을 갖는다.
        *
        * 두 요청은 따로 감싼다. 한 덩어리로 묶으면 한쪽이 실패할 때 멀쩡한 나머지 숫자까지
        * 사라진다. */}
      <Section
        title="운영 품질"
        description="최근 30일. 일이 밀렸는지와 잘 처리하고 있는지는 다른 질문입니다."
      >
        {/* 여기는 반반이다. 두 카드가 같은 급의 질문이라 한쪽을 넓히면 그쪽이 더 중요해
          * 보인다. 2:1 분할은 "넓은 쪽이 본문, 좁은 쪽이 곁들이"일 때만 쓴다. */}
        <div className="grid gap-4 xl:grid-cols-2">
          <Card>
            <CardHeader title="신고 처리 속도" description="접수부터 처리까지 걸린 시간" />
            <CardBody>
              <Loaded query={sla} skeleton={<RowSkeleton rows={3} />}>
                {(data) => (
                    <dl className="flex flex-col">
                      <MetricRow
                        label="가장 오래 기다린 건"
                        value={formatDurationMinutes(data.oldestPendingMinutes)}
                        hint="아직 처리 안 된 것 중 접수가 가장 이른 건"
                      />
                      <MetricRow
                        label="처리 시간 중앙값"
                        value={formatDurationMinutes(data.p50Minutes)}
                        hint={`30일 동안 처리한 ${data.resolvedCount}건 기준`}
                      />
                  </dl>
                )}
              </Loaded>
            </CardBody>
          </Card>

          <Card>
            <CardHeader title="검열 판정 정확도" description="AI 판정을 운영자가 뒤집은 비율" />
            <CardBody>
              <Loaded query={auditQuality} skeleton={<RowSkeleton rows={3} />}>
                {(data) => (
                  <dl className="flex flex-col">
                    <MetricRow
                      label="판정 뒤집힘"
                      value={formatPercent(data.overrideRate)}
                      hint="높아지면 모델을 손볼 때"
                    />
                    <MetricRow
                      label="오탐"
                      value={data.falsePositive}
                      hint="AI가 걸렀는데 운영자가 허용"
                    />
                    <MetricRow
                      label="미탐"
                      value={data.falseNegative}
                      hint="AI가 놓쳤는데 운영자가 차단"
                    />
                  </dl>
                )}
              </Loaded>
            </CardBody>
          </Card>
        </div>
      </Section>

      <Section title="서비스 흐름" description="오늘 숫자만으로는 0이 정상인지 알 수 없습니다.">
        {/* 왼쪽은 "어떻게 돌고 있나", 오른쪽은 "오늘 얼마나 됐나".
          * 추이는 가로가 길어야 모양이 보이고, 오늘 숫자는 세로로 쌓아야 자릿수가 비교된다. */}
        <div className={SPLIT}>
          <Card>
            <CardHeader title="최근 14일" actions={<TrendLegend />} />
            <CardBody>
              <Loaded query={trend} skeleton={<Skeleton className="h-[220px]" />}>
                {(data) => (
                  <Suspense fallback={<Skeleton className="h-[220px]" />}>
                    <TrendChart data={data} />
                  </Suspense>
                )}
              </Loaded>
            </CardBody>
          </Card>

          <Card>
            <CardHeader title="오늘" description={summary.data?.date} />
            <CardBody>
              <Loaded query={summary} skeleton={<RowSkeleton rows={4} />}>
                {(data) => (
                  <dl className="flex flex-col">
                    <MetricRow
                      label="방 생성"
                      value={data.funnel.created}
                      series={pick(series, 'created')}
                    />
                    <MetricRow
                      label="완주"
                      value={data.funnel.completed}
                      hint={`생성 대비 ${formatPercent(data.funnel.completionRate, 0)}`}
                      series={pick(series, 'completed')}
                    />
                    <MetricRow
                      label="참여자"
                      value={data.players}
                      hint="여러 방 참여 시 중복"
                      series={pick(series, 'players')}
                    />
                    {/* 신규 가입은 일자별 계열이 없다. 빈 배열을 줘서 그림 없이 자리만
                      * 잡는다. 다른 계열의 모양을 빌려 오면 그건 이 지표의 흐름이 아니고,
                      * 아예 안 주면 이 줄만 숫자가 왼쪽으로 밀린다. */}
                    <MetricRow
                      label="신규 가입"
                      value={data.signups}
                      hint="비회원도 게임은 가능"
                      series={[]}
                    />
                  </dl>
                )}
              </Loaded>
            </CardBody>
          </Card>
        </div>
      </Section>

      {/* 두 카드의 높이를 맞춘다.
        *
        * 한때 items-start 로 각자 내용만큼만 차지하게 뒀는데 나란히 선 카드의 아래가
        * 어긋나 보였다. 그렇다고 기본값(stretch)만 두면 짧은 퍼널 카드가 조치 목록
        * 높이까지 늘어나 아래 절반이 빈 흰 판이 된다.
        *
        * 양쪽에서 좁혔다. 조치는 다섯 건만 보여 목록을 짧게 하고, 퍼널은 남는 높이를
        * 위아래로 나눠 가운데 선다. 남는 30px 남짓이 위아래로 갈리면 여백으로 읽히지
        * 빈 판으로는 안 읽힌다. */}
      <div className={SPLIT}>
        <Card className="flex flex-col">
          <CardHeader
            title="오늘 방 진행 퍼널"
            description="게임 시작과 미니게임 완료의 차이가 하다가 나간 방입니다."
          />
          <CardBody className="flex flex-1 flex-col justify-center">
            <Loaded query={summary} skeleton={<RowSkeleton rows={5} height="h-7" />}>
              {(data) => (
                <FunnelBar
                  stages={[
                    { label: '방 생성', count: data.funnel.created },
                    { label: '게임 시작', count: data.funnel.gameStarted },
                    { label: '미니게임 완료', count: data.funnel.miniGamePlayed },
                    { label: '룰렛 도달', count: data.funnel.rouletteReached },
                    { label: '완주', count: data.funnel.completed },
                  ]}
                />
              )}
            </Loaded>
          </CardBody>
        </Card>

        <Card>
          <CardHeader
            title="최근 조치"
            description="Grafana 로는 볼 수 없는 기록입니다."
            actions={
              <Button asChild size="sm" variant="ghost">
                <Link to="/audit-logs">전체 보기</Link>
              </Button>
            }
          />
          <Loaded query={logs} skeleton={<RowSkeleton rows={5} />}>
            {(data) => <ActivityFeed logs={data.content} />}
          </Loaded>
        </Card>
      </div>
    </div>
  );
}

/** 추이 응답에서 계열 하나만 뽑는다. 스파크라인은 값 배열만 받는다. */
function pick(series: DailyTrend[], key: 'created' | 'completed' | 'players') {
  return series.map((day) => day[key]);
}

/**
 * 목록형 카드의 로딩 자리. 카드마다 Array.from 을 반복해 적던 것을 모았다.
 *
 * <p>좌우 여백이 없다. 전부 {@code CardBody} 안에서 쓰이므로 여백은 거기서 온다.
 * 예전에는 목록이 카드 직속이라 여기서 여백을 줬는데, 그때 값이 남아 있으면 여백이
 * 두 겹이 된다.
 */
function RowSkeleton({ rows, height = 'h-8' }: { rows: number; height?: string }) {
  return (
    <div className="flex flex-col gap-3 py-1">
      {Array.from({ length: rows }).map((_, index) => (
        <Skeleton key={index} className={height} />
      ))}
    </div>
  );
}

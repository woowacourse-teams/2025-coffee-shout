/**
 * 백엔드 응답 타입. <b>손으로 유지한다.</b>
 *
 * <p>한동안 `npm run gen:api`(springdoc → openapi-typescript)로 생성물로 갈아탈
 * 계획이 있었고 스크립트도 있었다. 걷어냈다. 세 가지가 걸렸다.
 *
 * <p>첫째, 생성하려면 서버가 떠 있어야 한다. CI 에서 백엔드를 띄워 스키마를 뽑는
 * 단계를 프론트 빌드 앞에 두면, 백엔드가 못 뜨는 날 프론트 배포까지 같이 멈춘다.
 *
 * <p>둘째, 생성 타입은 <b>모양만</b> 옮긴다. 여기 달린 "참여자는 사람이 아니라 참여
 * 건수다", "완료된 게임만 센다" 같은 주석이 이 파일의 값어치인데 생성물은 그걸 매번
 * 덮어쓴다. 화면을 만드는 사람이 실수하는 지점은 필드 이름이 아니라 <b>그 숫자가
 * 무엇을 세는가</b>다.
 *
 * <p>셋째, 백오피스 API 는 우리가 같은 PR 에서 양쪽을 고친다. 계약이 밖에서 바뀌는
 * 관계가 아니라서 자동 동기화로 얻는 게 적다.
 *
 * <p>대신 규칙이 하나 생긴다. <b>백엔드 DTO 를 고치면 여기도 같은 PR 에서 고친다.</b>
 * 안 고치면 화면이 조용히 빈칸을 그린다.
 */

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

/* ── 홈 대시보드 ─────────────────────────────────────────── */

export type ActionQueue = {
  pendingReports: number;
  flaggedNicknames: number;
  pendingNicknames: number;
  blockedIps: number;
  /**
   * 발행·소비에 실패해 격리된 메시지. 다른 넷과 성격이 다르다. 신고나 검열은 사람이
   * 판단해 줄 일이고, 이건 시스템이 멈춘 것이다. 정산 메시지 하나가 격리되면 그 방의
   * 정산은 영영 안 된다.
   */
  deadLetters: number;
  hasWork: boolean;
};

/** 작업함 한 줄의 출처. 화면이 이 값으로 칩과 이동할 곳을 정한다. */
export type InboxKind = 'REPORT' | 'NICKNAME' | 'DEAD_LETTER';

/**
 * 통합 작업함 한 줄.
 *
 * <p>세 출처를 한 모양으로 맞춘 것이다. 담긴 정보가 서로 다른데도 같은 네 칸으로
 * 줄인 이유는, 목록에서 묻는 것이 늘 같기 때문이다. 무엇에 대한 일이고, 왜 올라왔고,
 * 언제 들어왔는가.
 *
 * <p>서버가 <b>최신 20건</b>만 준다. 페이지가 없다. 이 화면은 전부를 훑는 자리가 아니라
 * 다음에 처리할 것을 보는 자리고, 전체 목록은 각 화면에 있다.
 */
export type InboxItem = {
  kind: InboxKind;
  /** 출처 안에서의 식별자. 격리 메시지만 `OUTBOX:3` 처럼 출처가 앞에 붙는다. */
  id: string;
  title: string;
  /** 왜 올라왔는지. 신고는 카테고리, 닉네임과 격리는 사유다. 없을 수 있다. */
  detail: string | null;
  occurredAt: string;
};

export type Funnel = {
  created: number;
  gameStarted: number;
  /** 미니게임을 한 판이라도 끝낸 방. 게임 시작과의 차이가 하다가 나간 방이다. */
  miniGamePlayed: number;
  rouletteReached: number;
  completed: number;
  completionRate: number;
};

export type DailyTrend = {
  date: string;
  created: number;
  completed: number;
  players: number;
};

export type GamePlayStat = {
  miniGameType: string;
  /** 화면에 찍는 한글 이름. 서버 enum 이 들고 있는 값을 그대로 받는다. */
  label: string;
  plays: number;
  /** 전체 대비 비중. 0.0 ~ 1.0 */
  share: number;
};

export type AdminAuditResult = 'SUCCESS' | 'FAILURE';

export type AdminAuditLog = {
  id: number;
  actorEmail: string;
  /** `DELETE /admin/api/ip-blocks/{ip}` 형태의 매핑 패턴. 화면이 문장으로 번역한다. */
  action: string;
  targetType: string | null;
  targetId: string | null;
  detail: string | null;
  result: AdminAuditResult;
  createdAt: string;
};

export type DailySummary = {
  date: string;
  funnel: Funnel;
  players: number;
  signups: number;
};

/**
 * 기간 합계. {@link DailySummary} 와 모양이 비슷하지만 따로 둔다.
 * 홈의 "오늘"은 날짜 하나를, 분석의 "최근 30일"은 구간을 말한다.
 */
export type PeriodSummary = {
  days: number;
  /** 포함 */
  from: string;
  /** 포함 */
  to: string;
  funnel: Funnel;
  players: number;
  signups: number;
  /** 분모는 생성된 방 전체다. 아무도 안 온 방도 포함이라 값이 낮으면 그게 신호다. */
  avgPlayersPerRoom: number;
};

/* ── 신고 ───────────────────────────────────────────────── */

export type ReportStatus = 'PENDING' | 'RESOLVED';

export type Report = {
  id: number;
  category: string;
  gameType: string | null;
  joinCode: string | null;
  content: string;
  status: ReportStatus;
  createdAt: string;
  resolvedAt: string | null;
  ip: string | null;
};

export type ReportSla = {
  resolvedCount: number;
  p50Minutes: number;
  p95Minutes: number;
  pendingCount: number;
  oldestPendingMinutes: number;
};

/* ── 닉네임 검열 ─────────────────────────────────────────── */

export type NicknameAuditStatus =
  | 'UNAUDITED'
  | 'FLAGGED'
  | 'PENDING'
  | 'CLEAN'
  | 'ALLOWED'
  | 'BLOCKED';

export type NicknameAudit = {
  id: number;
  nickname: string;
  status: NicknameAuditStatus;
  confidence: { value: number };
  reason: string;
  createdAt: string;
  auditedAt: string | null;
};

export type ProfanityWord = {
  word: string;
  language: 'KOREAN' | 'ENGLISH';
  source: string;
  active: boolean;
};

export type NicknameAuditQuality = {
  total: number;
  agreed: number;
  falsePositive: number;
  falseNegative: number;
  overrideRate: number;
};

/* ── IP 차단 ─────────────────────────────────────────────── */

export type BlockedIp = {
  ip: string;
  remainingTtlSeconds: number;
};

/* ── 방 ──────────────────────────────────────────────────── */

export type RoomState = 'READY' | 'PLAYING' | 'SCORE_BOARD' | 'ROULETTE' | 'DONE';

export type RoomSummary = {
  id: number;
  joinCode: string;
  status: RoomState;
  createdAt: string;
  finishedAt: string | null;
  playerCount: number;
};

export type RoomPlayer = {
  id: number;
  playerName: string;
  playerType: 'HOST' | 'GUEST';
  guest: boolean;
  userId: number | null;
  nickname: string | null;
  userCode: string | null;
  joinedAt: string;
};

export type RoomMiniGameResult = {
  miniGameType: string;
  playerId: number;
  playerName: string;
  rank: number;
  score: number | null;
  createdAt: string;
};

export type RoomRouletteResult = {
  winnerPlayerId: number;
  winnerPlayerName: string;
  winnerProbability: number;
  createdAt: string;
};

export type RoomDetail = {
  summary: RoomSummary;
  players: RoomPlayer[];
  miniGameResults: RoomMiniGameResult[];
  roulette: RoomRouletteResult | null;
};

/* ── 유저 ────────────────────────────────────────────────── */

export type UserSummary = {
  id: number;
  userCode: string;
  nickname: string | null;
  createdAt: string;
};

/**
 * 소셜 제공자 분포.
 *
 * <b>연결 수의 합은 회원 수와 다르다.</b> 한 사람이 구글과 카카오를 모두 연결할 수 있다.
 * 그래서 서버가 회원 수를 따로 준다. 합을 회원 수로 읽으면 다른 화면의 숫자와 어긋난다.
 */
export type ProviderStats = {
  /** 활성 회원 수. 탈퇴 회원은 빠진다. */
  userCount: number;
  /** 연결이 있는 제공자만. 많은 순. provider 는 소문자 google/kakao/naver. */
  providers: { provider: string; count: number }[];
};

export type UserDetail = {
  summary: UserSummary;
  providers: string[];
  roomCount: number;
  winCount: number;
  winRate: number;
};

/* ── 패치노트 ────────────────────────────────────────────── */

export type PatchNoteCategory = 'NOTICE' | 'EVENT' | 'UPDATE' | 'MAINTENANCE';

export type PatchNote = {
  id: number;
  category: PatchNoteCategory;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
};

/* ── 관리자 ──────────────────────────────────────────────── */

export type AdminAccount = {
  id: number | null;
  email: string;
  source: 'BOOTSTRAP' | 'DATABASE';
  removable: boolean;
  createdByEmail: string | null;
  createdAt: string | null;
};

export type AdminMe = { email: string };
export type AdminToken = { accessToken: string };

/* ── ZzolBot ─────────────────────────────────────────────── */

export type ZzolBotFeedback = 'GOOD' | 'BAD';

export type ZzolBotSession = {
  id: number;
  question: string;
  answer: string;
  feedback: ZzolBotFeedback | null;
  /** 서버가 "MM/dd HH:mm" 으로 이미 포맷한 문자열이다. ISO 가 아니라 Timestamp 에 못 넣는다. */
  createdAt: string;
};

export type MonitorAlert = {
  id: number;
  anomalous: boolean;
  severity: string;
  /** 원문 JSON 문자열. 서버가 파싱하지 않고 그대로 준다. */
  signalsJson: string | null;
  fingerprint: string | null;
  analysisSummary: string | null;
  suggestedActionsJson: string | null;
  notified: boolean;
  createdAt: string;
};

export type EvalRun = {
  id: number;
  label: string;
  model: string;
  status: string;
  scenarioCount: number;
  passCount: number;
  startedAt: string;
  finishedAt: string | null;
};

export type EvalResult = {
  scenarioId: number;
  accuracy: number;
  groundedness: number;
  hallucination: boolean;
  verdict: string;
  latencyMs: number;
  missingToolCalls: number;
  rationale: string | null;
  answer: string | null;
};

export type EvalRunDetail = {
  run: EvalRun;
  results: EvalResult[];
};

export type EvalScenario = {
  id: number;
  name: string;
  kind: string;
  question: string;
  rubric: string;
  sourceType: string;
  createdAt: string | null;
};

/* ── 시스템 운영 ─────────────────────────────────────────── */

export type DeadLetterSource = 'OUTBOX' | 'SETTLEMENT';

export type DeadLetter = {
  source: DeadLetterSource;
  id: number;
  /** 원본을 로그에서 되짚을 때 쓰는 값. outbox 는 스트림 키, 정산은 record_id. */
  reference: string;
  reason: string;
  /** 원문. 적체 건수는 Grafana 가 보여주지만 무엇이 왜 막혔는지는 이걸 봐야 안다. */
  payload: string | null;
  retryCount: number | null;
  /** 서버가 정한다. 프론트가 소스별 규칙표를 따로 들면 한쪽만 고치는 날이 온다. */
  requeueable: boolean;
  createdAt: string;
};

export type MigrationItem = {
  version: string | null;
  description: string;
  type: string;
  installedOn: string | null;
  success: boolean;
  executionTimeMs: number | null;
};

export type Migrations = {
  /** 이 환경이 Flyway 로 스키마를 관리하는지. 로컬은 ddl-auto 라 false 다. */
  managed: boolean;
  current: string | null;
  records: MigrationItem[];
};

export type Deployment = {
  version: string | null;
  commit: string | null;
  builtAt: string | null;
  profile: string;
};

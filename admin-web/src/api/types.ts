/**
 * 백엔드 응답 타입.
 *
 * <p>지금은 손으로 적었다. `npm run gen:api` 가 springdoc(`/v3/api-docs`)에서 타입을
 * 생성하지만 서버가 떠 있어야 하고, 그 산출물을 커밋하면 서버 없이 빌드가 안 되는 시점이
 * 생긴다. Phase 4 에서 CI 에 생성 단계를 넣으면서 이 파일을 생성물로 교체한다.
 *
 * <p>그때까지는 이 파일이 계약서다. 백엔드 DTO 를 고치면 여기도 고쳐야 하고,
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
  hasWork: boolean;
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

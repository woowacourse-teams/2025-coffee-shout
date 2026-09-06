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
  joined: number;
  gameStarted: number;
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

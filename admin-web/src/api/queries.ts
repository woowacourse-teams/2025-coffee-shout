import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '@/api/client';
import type {
  ActionQueue,
  AdminAccount,
  AdminAuditLog,
  BlockedIp,
  DailySummary,
  DailyTrend,
  GamePlayStat,
  NicknameAudit,
  NicknameAuditQuality,
  NicknameAuditStatus,
  PageResponse,
  PatchNote,
  PatchNoteCategory,
  PeriodSummary,
  ProfanityWord,
  Report,
  ReportSla,
  ReportStatus,
  RoomDetail,
  RoomSummary,
  UserDetail,
  UserSummary,
} from '@/api/types';

/**
 * 쿼리 키를 한 곳에 모은다. 조치 후 무엇을 다시 불러올지 정할 때 문자열을 흩어 두면
 * 화면 하나를 고칠 때마다 갱신이 빠지는 곳이 생긴다.
 */
export const keys = {
  overview: {
    queue: ['overview', 'queue'] as const,
    summary: (date?: string) => ['overview', 'summary', date ?? 'today'] as const,
  },
  reports: {
    list: (filters: unknown) => ['reports', 'list', filters] as const,
    sla: (days: number) => ['reports', 'sla', days] as const,
  },
  profanity: {
    audits: (status: NicknameAuditStatus, page: number) =>
      ['profanity', 'audits', status, page] as const,
    words: (filters: unknown) => ['profanity', 'words', filters] as const,
    quality: (days: number) => ['profanity', 'quality', days] as const,
  },
  ipBlocks: ['ip-blocks'] as const,
  rooms: {
    search: (joinCode: string, page: number) => ['rooms', 'search', joinCode, page] as const,
    detail: (roomId: number) => ['rooms', 'detail', roomId] as const,
  },
  users: {
    search: (keyword: string, page: number) => ['users', 'search', keyword, page] as const,
    detail: (userId: number) => ['users', 'detail', userId] as const,
  },
  patchNotes: ['patch-notes'] as const,
  admins: ['admins'] as const,
};

/* ── 홈 ─────────────────────────────────────────────────── */

export function useActionQueue() {
  return useQuery({
    queryKey: keys.overview.queue,
    queryFn: () => api.get<ActionQueue>('/overview/action-queue'),
    // 조치하고 돌아오면 바로 줄어들어야 한다. 30초면 표를 훑는 동안 한 번은 갱신된다.
    refetchInterval: 30_000,
  });
}

export function useTrend(days = 14) {
  return useQuery({
    queryKey: ['overview', 'trend', days],
    queryFn: () => api.get<DailyTrend[]>('/overview/trend', { days }),
  });
}

export function usePeriodSummary(days = 30) {
  return useQuery({
    queryKey: ['overview', 'period', days],
    queryFn: () => api.get<PeriodSummary>('/overview/period', { days }),
  });
}

export function useGamePlayStats(days = 30) {
  return useQuery({
    queryKey: ['overview', 'games', days],
    queryFn: () => api.get<GamePlayStat[]>('/overview/games', { days }),
  });
}

export function useAuditLogs(size = 20, page = 0) {
  return useQuery({
    queryKey: ['audit-logs', page, size],
    queryFn: () => api.get<PageResponse<AdminAuditLog>>('/audit-logs', { page, size }),
  });
}

export function useDailySummary(date?: string) {
  return useQuery({
    queryKey: keys.overview.summary(date),
    queryFn: () => api.get<DailySummary>('/overview/summary', { date }),
  });
}

/* ── 신고 ───────────────────────────────────────────────── */

type ReportFilters = {
  status?: ReportStatus;
  category?: string;
  gameType?: string;
  page: number;
};

export function useReports(filters: ReportFilters) {
  return useQuery({
    queryKey: keys.reports.list(filters),
    queryFn: () => api.get<PageResponse<Report>>('/reports', { ...filters }),
  });
}

export function useReportSla(days = 30) {
  return useQuery({
    queryKey: keys.reports.sla(days),
    queryFn: () => api.get<ReportSla>('/quality/report-sla', { days }),
  });
}

export function useResolveReport() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.post<void>(`/reports/${id}/resolve`),
    onSuccess: () => {
      // 목록과 대기 큐를 함께 갱신한다. 처리했는데 상단 숫자가 그대로면
      // 조치가 안 먹은 것으로 오해한다.
      client.invalidateQueries({ queryKey: ['reports'] });
      client.invalidateQueries({ queryKey: keys.overview.queue });
    },
  });
}

export function useUnblockReporterIp() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.delete<void>(`/reports/${id}/reporter-ip-block`),
    onSuccess: () => {
      client.invalidateQueries({ queryKey: keys.ipBlocks });
      client.invalidateQueries({ queryKey: keys.overview.queue });
    },
  });
}

/* ── 닉네임 검열 ─────────────────────────────────────────── */

export function useNicknameAudits(status: NicknameAuditStatus, page: number) {
  return useQuery({
    queryKey: keys.profanity.audits(status, page),
    queryFn: () => api.get<PageResponse<NicknameAudit>>('/profanity/audits', { status, page }),
  });
}

export function useAuditDecision() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ id, decision }: { id: number; decision: 'allow' | 'block' }) =>
      api.post<void>(`/profanity/audits/${id}/${decision}`),
    onSuccess: () => {
      client.invalidateQueries({ queryKey: ['profanity'] });
      client.invalidateQueries({ queryKey: keys.overview.queue });
    },
  });
}

type WordFilters = {
  search?: string;
  language?: string;
  source?: string;
  active?: boolean;
  page: number;
};

export function useProfanityWords(filters: WordFilters) {
  return useQuery({
    queryKey: keys.profanity.words(filters),
    queryFn: () => api.get<PageResponse<ProfanityWord>>('/profanity/words', { ...filters }),
  });
}

export function useAddProfanityWord() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (body: { word: string; language: ProfanityWord['language'] }) =>
      api.post<void>('/profanity/words', body),
    onSuccess: () => client.invalidateQueries({ queryKey: ['profanity', 'words'] }),
  });
}

/**
 * 단어를 지우지 않고 비활성으로 둔다. 삭제하면 왜 걸렸던 단어인지가 사라져
 * 같은 단어를 두 번 추가하고 두 번 푸는 일이 반복된다.
 */
export function useToggleProfanityWord() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ word, active }: { word: string; active: boolean }) =>
      active
        ? api.post<void>(`/profanity/words/${encodeURIComponent(word)}/activate`)
        : api.delete<void>(`/profanity/words/${encodeURIComponent(word)}/activate`),
    onSuccess: () => client.invalidateQueries({ queryKey: ['profanity', 'words'] }),
  });
}

export function useNicknameAuditQuality(days = 30) {
  return useQuery({
    queryKey: keys.profanity.quality(days),
    queryFn: () => api.get<NicknameAuditQuality>('/quality/nickname-audit', { days }),
  });
}

/* ── IP 차단 ─────────────────────────────────────────────── */

export function useBlockedIps() {
  return useQuery({
    queryKey: keys.ipBlocks,
    queryFn: () => api.get<BlockedIp[]>('/ip-blocks'),
  });
}

export function useUnblockIp() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (ip: string) => api.delete<void>(`/ip-blocks/${encodeURIComponent(ip)}`),
    onSuccess: () => {
      client.invalidateQueries({ queryKey: keys.ipBlocks });
      client.invalidateQueries({ queryKey: keys.overview.queue });
    },
  });
}

/* ── 방 ──────────────────────────────────────────────────── */

export function useRoomSearch(joinCode: string, page: number) {
  return useQuery({
    queryKey: keys.rooms.search(joinCode, page),
    queryFn: () => api.get<PageResponse<RoomSummary>>('/rooms', { joinCode, page }),
  });
}

export function useRoomDetail(roomId: number) {
  return useQuery({
    queryKey: keys.rooms.detail(roomId),
    queryFn: () => api.get<RoomDetail>(`/rooms/${roomId}`),
  });
}

/* ── 유저 ────────────────────────────────────────────────── */

export function useUserSearch(keyword: string, page: number) {
  return useQuery({
    queryKey: keys.users.search(keyword, page),
    queryFn: () => api.get<PageResponse<UserSummary>>('/users', { keyword, page }),
  });
}

export function useUserDetail(userId: number) {
  return useQuery({
    queryKey: keys.users.detail(userId),
    queryFn: () => api.get<UserDetail>(`/users/${userId}`),
  });
}

/* ── 패치노트 ────────────────────────────────────────────── */

export function usePatchNotes() {
  return useQuery({
    queryKey: keys.patchNotes,
    queryFn: () => api.get<PatchNote[]>('/patch-notes'),
  });
}

export function usePatchNote(id: number | null) {
  return useQuery({
    queryKey: ['patch-notes', 'detail', id],
    queryFn: () => api.get<PatchNote>(`/patch-notes/${id}`),
    // 새로 쓰는 화면에서는 부를 대상이 없다.
    enabled: id !== null,
  });
}

/**
 * 카테고리 선택지는 서버가 준다. 프론트에 enum 을 복사해 두면 서버가 값을 늘려도
 * 화면에 안 나오고, 반대로 없앤 값이 화면에 남아 저장할 때만 터진다.
 */
export function usePatchNoteCategories() {
  return useQuery({
    queryKey: ['patch-notes', 'categories'],
    queryFn: () => api.get<PatchNoteCategory[]>('/patch-notes/categories'),
    staleTime: Infinity,
  });
}

export type PatchNoteForm = {
  category: PatchNoteCategory;
  title: string;
  content: string;
};

export function useSavePatchNote() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ id, form }: { id: number | null; form: PatchNoteForm }) =>
      id === null
        ? api.post<PatchNote>('/patch-notes', form)
        : api.put<PatchNote>(`/patch-notes/${id}`, form),
    onSuccess: () => client.invalidateQueries({ queryKey: keys.patchNotes }),
  });
}

export function useDeletePatchNote() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.delete<void>(`/patch-notes/${id}`),
    onSuccess: () => client.invalidateQueries({ queryKey: keys.patchNotes }),
  });
}

/* ── 관리자 ──────────────────────────────────────────────── */

export function useAdminAccounts() {
  return useQuery({
    queryKey: keys.admins,
    queryFn: () => api.get<AdminAccount[]>('/accounts'),
  });
}

export function useAddAdminAccount() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (email: string) => api.post<AdminAccount>('/accounts', { email }),
    onSuccess: () => client.invalidateQueries({ queryKey: keys.admins }),
  });
}

export function useRemoveAdminAccount() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.delete<void>(`/accounts/${id}`),
    onSuccess: () => client.invalidateQueries({ queryKey: keys.admins }),
  });
}

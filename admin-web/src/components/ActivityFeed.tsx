import type { AdminAuditLog } from '@/api/types';
import { EmptyState } from '@/components/ui/EmptyState';
import { formatRelative } from '@/lib/format';
import { cn } from '@/lib/cn';

/**
 * 최근 관리자 조치. <b>Grafana 로는 절대 볼 수 없는 정보다.</b>
 *
 * <p>누가 방금 어떤 IP 를 풀었고 어떤 닉네임을 허용했는지는 우리 DB 에만 있다.
 * 운영자가 여럿이 되면 "내가 안 했는데 왜 풀렸지"가 첫 질문이 되고, 그 답이 여기 있다.
 *
 * <p>action 은 매핑 패턴(`DELETE /admin/api/ip-blocks/{ip}`)이라 사람이 읽기 나쁘다.
 * 화면에서 문장으로 바꾼다. 서버가 문장을 저장하면 문구를 고칠 때 과거 기록까지
 * 바뀌어 버려서, 저장은 기계가 읽는 형태로 두고 번역은 화면이 한다.
 *
 * <h2>구분선이 아니라 타임라인</h2>
 *
 * <p>줄마다 가로 구분선을 긋던 것을 걷어내고 왼쪽에 세로 실선을 세웠다. 이 목록의 성격은
 * "서로 무관한 항목 여럿"이 아니라 <b>시간 순으로 이어진 하나의 기록</b>이다. 가로선은
 * 항목을 끊어 놓아 그 성격을 지웠다. 세로선은 반대로 잇는다. 조치 하나가 다른 조치를
 * 되돌린 것인지(차단 → 해제) 읽으려면 그 이어짐이 보여야 한다.
 */
const ACTION_LABEL: Record<string, string> = {
  'POST /admin/api/auth/login': '로그인',
  'POST /admin/api/accounts': '관리자 추가',
  'DELETE /admin/api/accounts/{id}': '관리자 삭제',
  'DELETE /admin/api/ip-blocks/{ip}': 'IP 차단 해제',
  'POST /admin/api/reports/{id}/resolve': '신고 처리',
  'DELETE /admin/api/reports/{id}/reporter-ip-block': '신고자 IP 해제',
  'POST /admin/api/profanity/audits/{id}/allow': '닉네임 허용',
  'POST /admin/api/profanity/audits/{id}/block': '닉네임 차단',
  'POST /admin/api/profanity/words': '금칙어 추가',
  'DELETE /admin/api/patch-notes/{id}': '패치노트 삭제',
};

export function ActivityFeed({ logs }: { logs: AdminAuditLog[] }) {
  if (logs.length === 0) {
    return (
      <EmptyState
        title="최근 조치가 없습니다"
        description="관리자가 무언가를 바꾸면 여기에 남습니다."
      />
    );
  }

  return (
    <ul className="px-5 pb-5">
      {logs.map((log, index) => {
        const failed = log.result === 'FAILURE';
        const last = index === logs.length - 1;

        return (
          <li key={log.id} className="relative flex gap-3 pl-0.5">
            {/* 점과 선. 마지막 항목에서 선을 끊는다 - 아래로 이어질 것이 없다. */}
            <span className="relative flex w-3 shrink-0 justify-center">
              <span
                className={cn(
                  'z-10 mt-1.5 size-2 shrink-0 rounded-full ring-2 ring-surface',
                  failed ? 'bg-attention-mark' : 'bg-border-strong',
                )}
                aria-hidden
              />
              {!last && (
                <span className="absolute bottom-0 top-3 w-px bg-border-default" aria-hidden />
              )}
            </span>

            <div className={cn('min-w-0 flex-1', last ? 'pb-0' : 'pb-3')}>
              <p className="truncate text-sm text-ink">
                {ACTION_LABEL[log.action] ?? log.action}
                {log.targetId && (
                  <span className="ml-1.5 font-mono text-xs text-ink-muted">{log.targetId}</span>
                )}
                {failed && <span className="ml-1.5 text-xs text-attention">실패</span>}
              </p>
              <p className="truncate text-xs text-ink-muted">
                {log.actorEmail}
                <span className="mx-1.5 text-border-strong">·</span>
                {formatRelative(log.createdAt)}
              </p>
            </div>
          </li>
        );
      })}
    </ul>
  );
}

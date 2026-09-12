package coffeeshout.admin.quality.ui.response;

import coffeeshout.admin.quality.domain.ReportSla;

/**
 * 평균이 아니라 중앙값과 p95 다. 신고 하나가 며칠 방치되면 평균이 통째로 끌려가
 * "대체로 빠르다"와 "가끔 아주 느리다"를 구분하지 못한다.
 *
 * @param oldestPendingMinutes 가장 오래 기다린 미처리 신고의 나이. "지금 밀렸나"에 답한다.
 */
public record ReportSlaResponse(
        long resolvedCount, long p50Minutes, long p95Minutes, long pendingCount, long oldestPendingMinutes) {

    public static ReportSlaResponse from(ReportSla sla) {
        return new ReportSlaResponse(
                sla.resolvedCount(),
                sla.p50Minutes(),
                sla.p95Minutes(),
                sla.pendingCount(),
                sla.oldestPendingMinutes());
    }
}

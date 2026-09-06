package coffeeshout.admin.quality.domain;

/**
 * 신고 처리 소요 시간.
 *
 * <p>평균이 아니라 중앙값과 p95 를 쓴다. 신고 하나가 며칠 방치되면 평균이 통째로 끌려가
 * "대체로 빠르다"와 "가끔 아주 느리다"를 구분하지 못한다.
 *
 * @param oldestPendingMinutes 가장 오래 기다린 미처리 신고의 나이. 없으면 0.
 *                             적체 추이는 스냅샷이 없어 못 그리지만 이 값 하나로 "지금 밀렸나"는 답한다.
 */
public record ReportSla(
        long resolvedCount,
        long p50Minutes,
        long p95Minutes,
        long pendingCount,
        long oldestPendingMinutes
) {

    public static ReportSla empty() {
        return new ReportSla(0, 0, 0, 0, 0);
    }
}

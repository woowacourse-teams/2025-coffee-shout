package coffeeshout.admin.account.application;

import java.time.Instant;

/**
 * 관리자 목록 한 줄. 부트스트랩(환경변수)과 DB 항목을 한 형태로 합쳐 화면에 넘긴다.
 *
 * @param id            DB 항목만 값을 가진다. 부트스트랩은 null.
 * @param removable     UI에서 삭제 버튼을 노출할지. 부트스트랩은 항상 false.
 */
public record AdminAccountEntry(
        Long id,
        String email,
        AdminAccountSource source,
        boolean removable,
        String createdByEmail,
        Instant createdAt
) {

    public static AdminAccountEntry bootstrap(String email) {
        return new AdminAccountEntry(null, email, AdminAccountSource.BOOTSTRAP, false, null, null);
    }

    public static AdminAccountEntry database(
            Long id, String email, String createdByEmail, Instant createdAt) {
        return new AdminAccountEntry(
                id, email, AdminAccountSource.DATABASE, true, createdByEmail, createdAt);
    }
}

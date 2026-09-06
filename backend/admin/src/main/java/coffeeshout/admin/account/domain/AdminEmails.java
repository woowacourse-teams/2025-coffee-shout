package coffeeshout.admin.account.domain;

/**
 * 관리자 이메일 정규화.
 *
 * <p>허용목록 판정은 환경변수(부트스트랩)와 DB 두 곳에서 일어난다. 두 곳이 서로 다른 규칙으로
 * 이메일을 다루면 {@code Admin@Zzol.site}로 등록한 계정이 {@code admin@zzol.site}로 로그인할 때
 * 거부된다. 정규화 규칙을 한 곳에 두어 그 어긋남을 원천 차단한다.
 */
public final class AdminEmails {

    private AdminEmails() {
    }

    /**
     * 앞뒤 공백을 제거하고 소문자로 낮춘다. {@code null}이나 공백만 있는 값은 {@code null}을 돌려준다.
     */
    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        final String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase();
    }

    /**
     * 이미 정규화된 값인지 확인한다. 정규화를 거치지 않은 값이 저장 경로로 새어 들어오는 것을 막는다.
     */
    public static boolean isNormalized(String email) {
        return email != null && email.equals(normalize(email));
    }
}

package coffeeshout.admin.user.ui.response;

import coffeeshout.admin.user.domain.UserSummary;
import java.time.Instant;

/**
 * 이메일은 없다. 암호화 저장이라 복호화해야 읽히는데 백오피스가 그 열쇠를 쥘 이유가 없다.
 * 사람을 특정하는 데는 유저코드로 충분하다.
 *
 * <p>탈퇴 여부도 없다. {@code @SQLRestriction} 때문에 여기 오는 유저는 전부 활성 회원이라
 * 그 칸을 두면 언제나 같은 값이 찍힌다.
 */
public record UserSummaryResponse(
        Long id,
        String userCode,
        String nickname,
        Instant createdAt
) {

    public static UserSummaryResponse from(UserSummary summary) {
        return new UserSummaryResponse(
                summary.id(), summary.userCode(), summary.nickname(), summary.createdAt());
    }
}

package coffeeshout.admin.overview.domain;

import java.time.Instant;
import java.time.LocalDateTime;

public interface OverviewStatisticsRepository {

    RoomFunnel findFunnelBetween(LocalDateTime from, LocalDateTime to);

    long countPlayersBetween(LocalDateTime from, LocalDateTime to);

    /**
     * 가입 수만 {@code Instant}를 받는다. {@code app_user.created_at}이 Instant 컬럼이고
     * 방과 참여자는 LocalDateTime 이라 타입이 갈린다. 여기서 억지로 맞추면 어느 한쪽에
     * 시간대 변환이 숨는다. 스키마가 다르다는 사실을 시그니처에 드러낸다.
     */
    long countSignupsBetween(Instant from, Instant to);
}

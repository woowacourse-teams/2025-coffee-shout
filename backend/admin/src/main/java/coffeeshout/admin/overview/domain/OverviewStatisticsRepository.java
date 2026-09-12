package coffeeshout.admin.overview.domain;

import coffeeshout.minigame.domain.MiniGameType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface OverviewStatisticsRepository {

    RoomFunnel findFunnelBetween(LocalDateTime from, LocalDateTime to);

    long countPlayersBetween(LocalDateTime from, LocalDateTime to);

    /**
     * 가입 수만 {@code Instant}를 받는다. {@code app_user.created_at}이 Instant 컬럼이고
     * 방과 참여자는 LocalDateTime 이라 타입이 갈린다. 여기서 억지로 맞추면 어느 한쪽에
     * 시간대 변환이 숨는다. 스키마가 다르다는 사실을 시그니처에 드러낸다.
     */
    long countSignupsBetween(Instant from, Instant to);

    /**
     * 날짜별 흐름. 값이 없는 날은 빠져서 돌아온다. 빈 날을 0으로 채우는 것은
     * 서비스가 한다. 리포지토리가 달력을 만들기 시작하면 시간대 판단이 두 곳에 생긴다.
     */
    List<DailyTrendPoint> findDailyTrend(LocalDateTime from, LocalDateTime to);

    /** 완료된 게임만 센다. 비중 계산은 서비스가 한다. */
    List<GamePlayCount> countPlaysByGame(LocalDateTime from, LocalDateTime to);

    /** 비중을 계산하기 전의 원시 집계. */
    record GamePlayCount(MiniGameType miniGameType, long plays) {}
}

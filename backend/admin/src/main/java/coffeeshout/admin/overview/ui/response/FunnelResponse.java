package coffeeshout.admin.overview.ui.response;

import coffeeshout.admin.overview.domain.RoomFunnel;

/**
 * 방 진행 퍼널. 단계 이름은 화면 라벨과 1:1로 맞춘다.
 *
 * <p>SCORE_BOARD 단계는 없다. 그 상태는 DB로 내려가지 않아 언제나 0이 찍히는데,
 * 0이 늘 찍히는 칸이 표에 있으면 지표 전체를 못 믿게 된다.
 */
public record FunnelResponse(
        long created,
        long joined,
        long gameStarted,
        long rouletteReached,
        long completed,
        double completionRate
) {

    public static FunnelResponse from(RoomFunnel funnel) {
        return new FunnelResponse(
                funnel.created(),
                funnel.joined(),
                funnel.gameStarted(),
                funnel.rouletteReached(),
                funnel.completed(),
                funnel.completionRate());
    }
}

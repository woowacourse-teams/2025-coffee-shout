package coffeeshout.room.domain.event.broadcast;

import coffeeshout.room.domain.player.Winner;

/**
 * 룰렛 당첨자가 선택되었음을 알리는 Spring Domain Event
 * <p>
 * 호스트가 룰렛을 돌려서 당첨자가 결정되었을 때 발행됨
 * </p>
 * <p>
 * 이 이벤트를 수신하는 리스너는 WebSocket을 통해 당첨자 정보를 브로드캐스트함
 * </p>
 */
public record RouletteWinnerSelectedEvent(
        String joinCode,
        Winner winner
) {
}

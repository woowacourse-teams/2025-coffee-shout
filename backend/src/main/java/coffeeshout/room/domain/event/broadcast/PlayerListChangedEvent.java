package coffeeshout.room.domain.event.broadcast;

import coffeeshout.room.domain.player.Player;
import java.util.List;

/**
 * 플레이어 목록이 변경되었음을 알리는 Spring Domain Event
 * <p>
 * 다음 상황에서 발행됨:
 * <ul>
 *   <li>플레이어 준비 상태 변경</li>
 *   <li>플레이어 목록 업데이트 요청</li>
 *   <li>플레이어 강퇴</li>
 * </ul>
 * </p>
 * <p>
 * 이 이벤트를 수신하는 리스너는 WebSocket을 통해 변경된 플레이어 목록을 브로드캐스트함
 * </p>
 */
public record PlayerListChangedEvent(
        String joinCode,
        List<Player> players
) {
}

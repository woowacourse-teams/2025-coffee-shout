package coffeeshout.room.domain.event.broadcast;

import coffeeshout.minigame.domain.MiniGameType;
import java.util.List;

/**
 * 미니게임 목록이 변경되었음을 알리는 Spring Domain Event
 * <p>
 * 호스트가 미니게임을 선택하거나 변경할 때 발행됨
 * </p>
 * <p>
 * 이 이벤트를 수신하는 리스너는 WebSocket을 통해 선택된 미니게임 목록을 브로드캐스트함
 * </p>
 */
public record MiniGameListChangedEvent(
        String joinCode,
        List<MiniGameType> miniGameTypes
) {
}

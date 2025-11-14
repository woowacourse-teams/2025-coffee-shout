package coffeeshout.room.domain.event.broadcast;

import coffeeshout.room.domain.RoomState;

/**
 * 룰렛 화면이 표시되었음을 알리는 Spring Domain Event
 * <p>
 * 호스트가 룰렛 페이지로 전환할 때 발행됨
 * </p>
 * <p>
 * 이 이벤트를 수신하는 리스너는 WebSocket을 통해 룰렛 화면 전환을 브로드캐스트함
 * </p>
 */
public record RouletteShownEvent(
        String joinCode,
        RoomState roomState
) {
}

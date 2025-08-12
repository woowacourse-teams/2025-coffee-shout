package coffeeshout.global.websocket.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RoomStateUpdateEvent {
    private final String joinCode;
    private final String reason; // "PLAYER_REMOVED", "PLAYER_RECONNECTED" 등
}

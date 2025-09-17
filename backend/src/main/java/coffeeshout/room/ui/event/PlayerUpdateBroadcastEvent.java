package coffeeshout.room.ui.event;

import coffeeshout.room.ui.response.PlayerResponse;
import java.util.List;

public record PlayerUpdateBroadcastEvent(
        String joinCode,
        List<PlayerResponse> players
) implements BroadcastEvent {

    public static PlayerUpdateBroadcastEvent create(final String joinCode, final List<PlayerResponse> players) {
        return new PlayerUpdateBroadcastEvent(joinCode, players);
    }

    @Override
    public BroadcastEventType getBroadcastEventType() {
        return BroadcastEventType.PLAYER_UPDATE;
    }
}

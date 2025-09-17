package coffeeshout.room.ui.event;

import coffeeshout.minigame.domain.MiniGameType;
import java.util.List;

public record MiniGameUpdateBroadcastEvent(
        String joinCode,
        List<MiniGameType> miniGameTypes
) implements BroadcastEvent {

    public static MiniGameUpdateBroadcastEvent create(final String joinCode, final List<MiniGameType> miniGameTypes) {
        return new MiniGameUpdateBroadcastEvent(joinCode, miniGameTypes);
    }

    @Override
    public BroadcastEventType getBroadcastEventType() {
        return BroadcastEventType.MINI_GAME_UPDATE;
    }
}

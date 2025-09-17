package coffeeshout.room.ui.event;

public record MiniGameUpdateBroadcastEvent(
        String joinCode
) implements BroadcastEvent {

    public static MiniGameUpdateBroadcastEvent create(String joinCode) {
        return new MiniGameUpdateBroadcastEvent(joinCode);
    }

    @Override
    public BroadcastEventType getBroadcastEventType() {
        return BroadcastEventType.MINI_GAME_UPDATE;
    }
}

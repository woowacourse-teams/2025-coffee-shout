package coffeeshout.room.ui.event;

public record PlayerUpdateBroadcastEvent(
        String joinCode
) implements BroadcastEvent {

    public static PlayerUpdateBroadcastEvent create(final String joinCode) {
        return new PlayerUpdateBroadcastEvent(joinCode);
    }

    @Override
    public BroadcastEventType getBroadcastEventType() {
        return BroadcastEventType.PLAYER_UPDATE;
    }
}

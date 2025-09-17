package coffeeshout.room.ui.event;

public record ErrorBroadcastEvent(
        String joinCode,
        String errorMessage,
        String destination
) implements BroadcastEvent {

    public static ErrorBroadcastEvent create(final String joinCode, final String errorMessage, final String destination) {
        return new ErrorBroadcastEvent(joinCode, errorMessage, destination);
    }

    @Override
    public BroadcastEventType getBroadcastEventType() {
        return BroadcastEventType.ERROR;
    }
}

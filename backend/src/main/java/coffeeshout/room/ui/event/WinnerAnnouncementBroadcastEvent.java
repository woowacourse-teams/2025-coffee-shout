package coffeeshout.room.ui.event;

import coffeeshout.room.ui.response.WinnerResponse;

public record WinnerAnnouncementBroadcastEvent(
        String joinCode,
        WinnerResponse winner
) implements BroadcastEvent {

    public static WinnerAnnouncementBroadcastEvent create(final String joinCode, final WinnerResponse winner) {
        return new WinnerAnnouncementBroadcastEvent(joinCode, winner);
    }

    @Override
    public BroadcastEventType getBroadcastEventType() {
        return BroadcastEventType.WINNER_ANNOUNCEMENT;
    }
}

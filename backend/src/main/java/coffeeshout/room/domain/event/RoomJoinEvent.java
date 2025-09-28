package coffeeshout.room.domain.event;

import coffeeshout.global.event.BaseEvent;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import lombok.Getter;

@Getter
public class RoomJoinEvent extends BaseEvent implements RoomBaseEvent {
    private final RoomEventType eventType;
    private final String joinCode;
    private final String guestName;
    private final SelectedMenuRequest selectedMenuRequest;
    private final int guestColorIndex;

    public RoomJoinEvent(String joinCode, String guestName, SelectedMenuRequest selectedMenuRequest, int guestColorIndex) {
        this.eventType = RoomEventType.ROOM_JOIN;
        this.joinCode = joinCode;
        this.guestName = guestName;
        this.selectedMenuRequest = selectedMenuRequest;
        this.guestColorIndex = guestColorIndex;
    }

    @Override
    public RoomEventType getEventType() {
        return eventType;
    }
}

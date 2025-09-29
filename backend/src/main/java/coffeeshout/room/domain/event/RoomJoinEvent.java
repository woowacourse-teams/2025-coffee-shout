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

    public RoomJoinEvent(String joinCode, String guestName, SelectedMenuRequest selectedMenuRequest) {
        this.eventType = RoomEventType.ROOM_JOIN;
        this.joinCode = joinCode;
        this.guestName = guestName;
        this.selectedMenuRequest = selectedMenuRequest;
    }

    @Override
    public RoomEventType getEventType() {
        return eventType;
    }
}

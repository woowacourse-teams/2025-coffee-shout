package coffeeshout.room.domain.event;

import coffeeshout.global.event.BaseEvent;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import lombok.Getter;

@Getter
public class RoomCreateEvent extends BaseEvent implements RoomBaseEvent {
    private final RoomEventType eventType;
    private final String hostName;
    private final SelectedMenuRequest selectedMenuRequest;
    private final String joinCode;

    public RoomCreateEvent(String hostName, SelectedMenuRequest selectedMenuRequest, String joinCode) {
        this.eventType = RoomEventType.ROOM_CREATE;
        this.hostName = hostName;
        this.selectedMenuRequest = selectedMenuRequest;
        this.joinCode = joinCode;
    }

    @Override
    public RoomEventType getEventType() {
        return eventType;
    }
}

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
    private final int hostColorIndex;

    public RoomCreateEvent(String hostName, SelectedMenuRequest selectedMenuRequest, String joinCode, int hostColorIndex) {
        this.eventType = RoomEventType.ROOM_CREATE;
        this.hostName = hostName;
        this.selectedMenuRequest = selectedMenuRequest;
        this.joinCode = joinCode;
        this.hostColorIndex = hostColorIndex;
    }

    @Override
    public RoomEventType getEventType() {
        return eventType;
    }
}

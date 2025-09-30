package coffeeshout.room.domain.event;

import coffeeshout.global.event.BaseEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class PlayerListUpdateEvent extends BaseEvent implements RoomBaseEvent {
    private final RoomEventType eventType;
    private final String joinCode;

    public PlayerListUpdateEvent(String joinCode) {
        this.eventType = RoomEventType.PLAYER_LIST_UPDATE;
        this.joinCode = joinCode;
    }

    @Override
    public Instant getTimeStamp() {
        return timestamp;
    }

    @Override
    public RoomEventType getEventType() {
        return eventType;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
}

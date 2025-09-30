package coffeeshout.room.domain.event;

import coffeeshout.global.event.BaseEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class RouletteShowEvent extends BaseEvent implements RoomBaseEvent {
    private final RoomEventType eventType;
    private final String joinCode;

    public RouletteShowEvent(String joinCode) {
        this.eventType = RoomEventType.ROULETTE_SHOW;
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

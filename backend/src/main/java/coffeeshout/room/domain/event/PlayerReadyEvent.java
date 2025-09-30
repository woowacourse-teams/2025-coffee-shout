package coffeeshout.room.domain.event;

import coffeeshout.global.event.BaseEvent;
import java.time.Instant;
import lombok.Getter;

@Getter
public class PlayerReadyEvent extends BaseEvent implements RoomBaseEvent {
    private final RoomEventType eventType;
    private final String joinCode;
    private final String playerName;
    private final Boolean isReady;

    public PlayerReadyEvent(String joinCode, String playerName, Boolean isReady) {
        this.eventType = RoomEventType.PLAYER_READY;
        this.joinCode = joinCode;
        this.playerName = playerName;
        this.isReady = isReady;
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

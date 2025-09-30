package coffeeshout.room.domain.event;

import coffeeshout.global.event.BaseEvent;
import coffeeshout.minigame.domain.MiniGameType;
import java.time.Instant;
import java.util.List;
import lombok.Getter;

@Getter
public class MiniGameSelectEvent extends BaseEvent implements RoomBaseEvent {
    private final RoomEventType eventType;
    private final String joinCode;
    private final String hostName;
    private final List<MiniGameType> miniGameTypes;

    public MiniGameSelectEvent(String joinCode, String hostName, List<MiniGameType> miniGameTypes) {
        this.eventType = RoomEventType.MINI_GAME_SELECT;
        this.joinCode = joinCode;
        this.hostName = hostName;
        this.miniGameTypes = miniGameTypes;
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

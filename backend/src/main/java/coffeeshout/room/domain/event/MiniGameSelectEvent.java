package coffeeshout.room.domain.event;

import coffeeshout.minigame.domain.MiniGameType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MiniGameSelectEvent(
        String eventId,
        RoomEventType eventType,
        String joinCode,
        String hostName,
        List<MiniGameType> miniGameTypes,
        LocalDateTime timestamp
) implements RoomBaseEvent {

    public static MiniGameSelectEvent create(String joinCode, String hostName, List<MiniGameType> miniGameTypes) {
        return new MiniGameSelectEvent(
                UUID.randomUUID().toString(),
                RoomEventType.MINI_GAME_SELECT,
                joinCode,
                hostName,
                miniGameTypes,
                LocalDateTime.now()
        );
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public RoomEventType getEventType() {
        return eventType;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

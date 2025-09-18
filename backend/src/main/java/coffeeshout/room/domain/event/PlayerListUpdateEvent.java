package coffeeshout.room.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlayerListUpdateEvent(
        String eventId,
        RoomEventType eventType,
        String joinCode,
        LocalDateTime timestamp
) implements BaseEvent {
    
    public static PlayerListUpdateEvent create(String joinCode) {
        return new PlayerListUpdateEvent(
                UUID.randomUUID().toString(),
                RoomEventType.PLAYER_LIST_UPDATE,
                joinCode,
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

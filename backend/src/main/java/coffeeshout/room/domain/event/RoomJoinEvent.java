package coffeeshout.room.domain.event;

import coffeeshout.room.ui.request.SelectedMenuRequest;
import java.time.LocalDateTime;
import java.util.UUID;

public record RoomJoinEvent(
        String eventId,
        RoomEventType eventType,
        String joinCode,
        String guestName,
        SelectedMenuRequest selectedMenuRequest,
        LocalDateTime timestamp
) implements BaseEvent {
    
    public static RoomJoinEvent create(String joinCode, String guestName, SelectedMenuRequest selectedMenuRequest) {
        return new RoomJoinEvent(
                UUID.randomUUID().toString(),
                RoomEventType.ROOM_JOIN,
                joinCode,
                guestName,
                selectedMenuRequest,
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

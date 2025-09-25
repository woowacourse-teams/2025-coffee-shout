package coffeeshout.room.domain.event;

import coffeeshout.room.ui.request.SelectedMenuRequest;
import java.time.LocalDateTime;
import java.util.UUID;

public record RoomCreateEvent(
        String eventId,
        RoomEventType eventType,
        String hostName,
        SelectedMenuRequest selectedMenuRequest,
        String joinCode,
        LocalDateTime timestamp
) implements RoomBaseEvent {

    public static RoomCreateEvent create(String hostName, SelectedMenuRequest selectedMenuRequest, String joinCode) {
        return new RoomCreateEvent(
                UUID.randomUUID().toString(),
                RoomEventType.ROOM_CREATE,
                hostName,
                selectedMenuRequest,
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

package coffeeshout.room.domain.event;

import coffeeshout.global.redis.UserEvent;
import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import coffeeshout.room.ui.request.SelectedMenuRequest;
import java.time.Instant;
import java.util.UUID;

public record RoomJoinEvent(
        String userName,
        String eventId,
        TraceInfo traceInfo,
        Instant timestamp,
        RoomEventType eventType,
        String joinCode,
        String guestName,
        SelectedMenuRequest selectedMenuRequest
) implements UserEvent, Traceable {

    public RoomJoinEvent(String joinCode, String guestName, SelectedMenuRequest selectedMenuRequest) {
        this(
                "none",
                UUID.randomUUID().toString(),
                TraceInfoExtractor.extract(),
                Instant.now(),
                RoomEventType.ROOM_JOIN,
                joinCode,
                guestName,
                selectedMenuRequest
        );
    }
}

package coffeeshout.room.domain.event;

import coffeeshout.global.redis.UserEvent;
import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import coffeeshout.global.websocket.SynchronizedWebsocketInfo;
import java.time.Instant;
import java.util.UUID;

public record PlayerReadyEvent(
        String userName,
        String eventId,
        TraceInfo traceInfo,
        Instant timestamp,
        RoomEventType eventType,
        String joinCode,
        String playerName,
        Boolean isReady
) implements UserEvent, Traceable {

    public PlayerReadyEvent(String joinCode, String playerName, Boolean isReady) {
        this(
                SynchronizedWebsocketInfo.getUserName(),
                UUID.randomUUID().toString(),
                TraceInfoExtractor.extract(),
                Instant.now(),
                RoomEventType.PLAYER_READY,
                joinCode,
                playerName,
                isReady
        );
    }
}

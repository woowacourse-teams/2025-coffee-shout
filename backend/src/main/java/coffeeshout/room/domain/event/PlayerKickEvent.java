package coffeeshout.room.domain.event;

import coffeeshout.global.redis.UserEvent;
import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import coffeeshout.global.websocket.SynchronizedWebsocketInfo;
import java.time.Instant;
import java.util.UUID;

public record PlayerKickEvent(
        String userName,
        String eventId,
        TraceInfo traceInfo,
        Instant timestamp,
        RoomEventType eventType,
        String joinCode,
        String playerName
) implements UserEvent, Traceable {

    public PlayerKickEvent(String joinCode, String playerName) {
        this(
                SynchronizedWebsocketInfo.getUserName(),
                UUID.randomUUID().toString(),
                TraceInfoExtractor.extract(),
                Instant.now(),
                RoomEventType.PLAYER_KICK,
                joinCode,
                playerName
        );
    }
}

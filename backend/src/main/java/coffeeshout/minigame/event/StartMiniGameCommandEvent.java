package coffeeshout.minigame.event;

import coffeeshout.global.redis.UserEvent;
import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import coffeeshout.global.websocket.SynchronizedWebsocketInfo;
import java.time.Instant;
import java.util.UUID;

public record StartMiniGameCommandEvent(
        String userName,
        String eventId,
        TraceInfo traceInfo,
        Instant timestamp,
        String joinCode,
        String hostName
) implements UserEvent, Traceable {

    public StartMiniGameCommandEvent(String joinCode, String hostName) {
        this(
                SynchronizedWebsocketInfo.getWebsocketInfo().getHeaders().get("simpSessionId", String.class),
                UUID.randomUUID().toString(),
                TraceInfoExtractor.extract(),
                Instant.now(),
                joinCode,
                hostName
        );
    }
}

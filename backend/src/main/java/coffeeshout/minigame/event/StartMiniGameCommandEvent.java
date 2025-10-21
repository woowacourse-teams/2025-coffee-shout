package coffeeshout.minigame.event;

import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import coffeeshout.global.websocket.SynchronizedWebsocketInfo;
import java.time.Instant;
import java.util.UUID;
//
public record StartMiniGameCommandEvent(
        String sessionId,
        String eventId,
        TraceInfo traceInfo,
        Instant timestamp,
        MiniGameEventType eventType,
        String joinCode,
        String hostName
) implements MiniGameBaseEvent, Traceable {

    public StartMiniGameCommandEvent(String joinCode, String hostName) {
        this(
                SynchronizedWebsocketInfo.getWebsocketInfo().getHeaders().get("simpSessionId", String.class),
                UUID.randomUUID().toString(),
                TraceInfoExtractor.extract(),
                Instant.now(),
                MiniGameEventType.START_MINIGAME_COMMAND,
                joinCode,
                hostName
        );
    }

    @Override
    public TraceInfo getTraceInfo() {
        return traceInfo;
    }
}

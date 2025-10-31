package coffeeshout.racinggame.domain.event;

import coffeeshout.global.redis.UserEvent;
import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import coffeeshout.global.websocket.SynchronizedWebsocketInfo;
import java.time.Instant;
import java.util.UUID;

public record TapCommandEvent(
        String userName,
        String eventId,
        RacingGameEventType eventType,
        String joinCode,
        String playerName,
        int tapCount,
        Instant timestamp,
        TraceInfo traceInfo
) implements UserEvent, Traceable {

    public static TapCommandEvent create(String joinCode, String playerName, int tapCount) {
        final String eventId = UUID.randomUUID().toString();
        return new TapCommandEvent(
                SynchronizedWebsocketInfo.getUserName(),
                eventId,
                RacingGameEventType.TAP_COMMAND,
                joinCode,
                playerName,
                tapCount,
                Instant.now(),
                TraceInfoExtractor.extract()
        );
    }
}

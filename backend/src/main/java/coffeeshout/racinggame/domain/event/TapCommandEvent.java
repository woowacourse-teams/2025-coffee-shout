package coffeeshout.racinggame.domain.event;

import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TapCommandEvent(
        String eventId,
        RacingGameEventType eventType,
        LocalDateTime createdAt,
        String joinCode,
        String playerName,
        int tapCount,
        Instant timestamp,
        TraceInfo traceInfo
) {

    public static TapCommandEvent create(String joinCode, String playerName, int tapCount, Instant timestamp) {
        final String eventId = UUID.randomUUID().toString();
        return new TapCommandEvent(
                eventId,
                RacingGameEventType.TAP_COMMAND,
                LocalDateTime.now(),
                joinCode,
                playerName,
                tapCount,
                timestamp,
                TraceInfoExtractor.extract()
        );
    }
}

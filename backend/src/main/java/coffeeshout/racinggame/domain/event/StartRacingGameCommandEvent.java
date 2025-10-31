package coffeeshout.racinggame.domain.event;

import coffeeshout.global.redis.UserEvent;
import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import coffeeshout.global.websocket.SynchronizedWebsocketInfo;
import java.time.Instant;
import java.util.UUID;

public record StartRacingGameCommandEvent(
        String userName,
        String eventId,
        RacingGameEventType eventType,
        Instant timestamp,
        TraceInfo traceInfo,
        String joinCode,
        String hostName
) implements UserEvent, Traceable {

    public static StartRacingGameCommandEvent create(String joinCode, String hostName) {
        return new StartRacingGameCommandEvent(
                SynchronizedWebsocketInfo.getUserName(),
                UUID.randomUUID().toString(),
                RacingGameEventType.START_RACING_GAME_COMMAND,
                Instant.now(),
                TraceInfoExtractor.extract(),
                joinCode,
                hostName
        );
    }
}

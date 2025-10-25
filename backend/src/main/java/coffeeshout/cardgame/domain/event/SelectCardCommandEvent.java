package coffeeshout.cardgame.domain.event;

import coffeeshout.global.redis.UserEvent;
import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import coffeeshout.global.websocket.SynchronizedWebsocketInfo;
import java.time.Instant;
import java.util.UUID;

public record SelectCardCommandEvent(
        String userName,
        String eventId,
        TraceInfo traceInfo,
        Instant timestamp,
        String joinCode,
        String playerName,
        Integer cardIndex
) implements UserEvent, Traceable {

    public SelectCardCommandEvent(String joinCode, String playerName, Integer cardIndex) {
        this(
                SynchronizedWebsocketInfo.getUserName(),
                UUID.randomUUID().toString(),
                TraceInfoExtractor.extract(),
                Instant.now(),
                joinCode,
                playerName,
                cardIndex
        );
    }
}

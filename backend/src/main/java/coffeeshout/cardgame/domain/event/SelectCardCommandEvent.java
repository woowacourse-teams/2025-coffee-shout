package coffeeshout.cardgame.domain.event;

import coffeeshout.global.websocket.SynchronizedWebsocketInfo;
import coffeeshout.minigame.event.MiniGameBaseEvent;
import coffeeshout.minigame.event.MiniGameEventType;
import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import java.time.Instant;
import java.util.UUID;

public record SelectCardCommandEvent(
        String sessionId,
        String eventId,
        TraceInfo traceInfo,
        Instant timestamp,
        MiniGameEventType eventType,
        String joinCode,
        String playerName,
        Integer cardIndex
) implements MiniGameBaseEvent, Traceable {

    public SelectCardCommandEvent(String joinCode, String playerName, Integer cardIndex) {
        this(
                SynchronizedWebsocketInfo.getWebsocketInfo().getHeaders().get("simpSessionId", String.class),
                UUID.randomUUID().toString(),
                TraceInfoExtractor.extract(),
                Instant.now(),
                MiniGameEventType.SELECT_CARD_COMMAND,
                joinCode,
                playerName,
                cardIndex
        );
    }

    @Override
    public TraceInfo getTraceInfo() {
        return traceInfo;
    }
}

package coffeeshout.minigame.cardgame.domain.cardgame.event;

import coffeeshout.minigame.cardgame.domain.event.MiniGameBaseEvent;
import coffeeshout.minigame.cardgame.domain.event.MiniGameEventType;
import java.time.LocalDateTime;
import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import java.time.Instant;
import java.util.UUID;

public record SelectCardCommandEvent(
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

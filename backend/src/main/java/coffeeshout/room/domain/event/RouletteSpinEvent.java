package coffeeshout.room.domain.event;

import coffeeshout.global.redis.UserEvent;
import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import coffeeshout.global.websocket.SynchronizedWebsocketInfo;
import coffeeshout.room.domain.player.Winner;
import java.time.Instant;
import java.util.UUID;

public record RouletteSpinEvent(
        String userName,
        String eventId,
        TraceInfo traceInfo,
        Instant timestamp,
        RoomEventType eventType,
        String joinCode,
        String hostName,
        Winner winner
) implements UserEvent, Traceable {

    public RouletteSpinEvent(String joinCode, String hostName, Winner winner) {
        this(
                SynchronizedWebsocketInfo.getUserName(),
                UUID.randomUUID().toString(),
                TraceInfoExtractor.extract(),
                Instant.now(),
                RoomEventType.ROULETTE_SPIN,
                joinCode,
                hostName,
                winner
        );
    }
}

package coffeeshout.minigame.domain.event;

import coffeeshout.global.event.TraceInfo;
import java.time.Instant;

public interface MiniGameBaseEvent {
    String eventId();

    TraceInfo traceInfo();

    Instant timestamp();

    MiniGameEventType eventType();
}

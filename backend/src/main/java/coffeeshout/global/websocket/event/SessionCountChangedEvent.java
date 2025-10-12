package coffeeshout.global.websocket.event;

import coffeeshout.global.trace.TraceInfo;
import coffeeshout.global.trace.TraceInfoExtractor;
import coffeeshout.global.trace.Traceable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WebSocket 세션 수 변경 이벤트
 * <p>
 * 세션이 연결되거나 해제될 때 발행되는 이벤트입니다.
 * </p>
 */
public record SessionCountChangedEvent(
        String eventId,
        TraceInfo traceInfo,
        LocalDateTime timestamp,
        SessionChangeType changeType,
        String sessionId,
        int remainingSessionCount
) implements Traceable {

    public SessionCountChangedEvent {
        if (remainingSessionCount < 0) {
            throw new IllegalArgumentException("remainingSessionCount는 0 이상이어야 합니다: " + remainingSessionCount);
        }
    }

    public static SessionCountChangedEvent connected(String sessionId, int remainingCount) {
        return new SessionCountChangedEvent(
                UUID.randomUUID().toString(),
                TraceInfoExtractor.extract(),
                LocalDateTime.now(),
                SessionChangeType.CONNECTED,
                sessionId,
                remainingCount
        );
    }

    public static SessionCountChangedEvent disconnected(String sessionId, int remainingCount) {
        return new SessionCountChangedEvent(
                UUID.randomUUID().toString(),
                TraceInfoExtractor.extract(),
                LocalDateTime.now(),
                SessionChangeType.DISCONNECTED,
                sessionId,
                remainingCount
        );
    }

    @Override
    public TraceInfo getTraceInfo() {
        return traceInfo;
    }

    public enum SessionChangeType {
        CONNECTED,
        DISCONNECTED
    }
}

package coffeeshout.global.websocket.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * WebSocket 세션 수 변경 이벤트
 * <p>
 * 세션이 연결되거나 해제될 때 발행되는 이벤트입니다.
 * </p>
 */
@Getter
public class SessionCountChangedEvent extends ApplicationEvent {

    private final int remainingSessionCount;
    private final String sessionId;
    private final ChangeType changeType;

    private SessionCountChangedEvent(Object source, String sessionId, int remainingSessionCount, ChangeType changeType) {
        super(source);
        this.sessionId = sessionId;
        this.remainingSessionCount = remainingSessionCount;
        this.changeType = changeType;
    }

    public static SessionCountChangedEvent connected(Object source, String sessionId, int remainingCount) {
        return new SessionCountChangedEvent(source, sessionId, remainingCount, ChangeType.CONNECTED);
    }

    public static SessionCountChangedEvent disconnected(Object source, String sessionId, int remainingCount) {
        return new SessionCountChangedEvent(source, sessionId, remainingCount, ChangeType.DISCONNECTED);
    }

    public enum ChangeType {
        CONNECTED,
        DISCONNECTED
    }
}

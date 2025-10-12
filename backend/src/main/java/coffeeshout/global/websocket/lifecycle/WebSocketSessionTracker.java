package coffeeshout.global.websocket.lifecycle;

import coffeeshout.global.websocket.event.SessionCountChangedEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * 현재 인스턴스의 WebSocket 세션을 추적하는 컴포넌트
 * <p>
 * Redis pub/sub과 독립적으로 동작하며, Graceful Shutdown을 위한 세션 카운팅만 담당합니다.
 * AtomicInteger를 사용하여 경쟁 조건 없이 세션 수를 추적합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSessionTracker {

    private final ApplicationEventPublisher eventPublisher;

    // 현재 인스턴스에 연결된 모든 WebSocket 세션 ID
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();

    // 활성 세션 수 (경쟁 조건 방지)
    private final AtomicInteger activeSessionCount = new AtomicInteger(0);

    /**
     * WebSocket 연결 시 세션 추가
     */
    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        final String sessionId = extractSessionId(event);
        if (sessionId == null) {
            log.warn("SessionConnectEvent에서 sessionId를 추출할 수 없습니다");
            return;
        }

        // 세션이 새로 추가된 경우에만 카운트 증가
        final boolean added = activeSessions.add(sessionId);
        if (added) {
            final int currentCount = activeSessionCount.incrementAndGet();
            log.debug("WebSocket 세션 추가: sessionId={}, 총 활성 세션 수={}", sessionId, currentCount);

            // 세션 수 변경 이벤트 발행
            eventPublisher.publishEvent(SessionCountChangedEvent.connected(sessionId, currentCount));
        } else {
            log.warn("이미 존재하는 세션 ID: sessionId={}", sessionId);
        }
    }

    /**
     * WebSocket 연결 해제 시 세션 제거
     */
    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        final String sessionId = event.getSessionId();

        // 세션이 실제로 제거된 경우에만 카운트 감소
        final boolean removed = activeSessions.remove(sessionId);
        if (removed) {
            final int remainingCount = activeSessionCount.decrementAndGet();
            log.debug("WebSocket 세션 제거: sessionId={}, 남은 활성 세션 수={}", sessionId, remainingCount);

            // 세션 수 변경 이벤트 발행
            eventPublisher.publishEvent(SessionCountChangedEvent.disconnected(sessionId, remainingCount));
        } else {
            log.warn("존재하지 않는 세션 ID 제거 시도: sessionId={}", sessionId);
        }
    }

    /**
     * 현재 활성 WebSocket 세션 수 조회
     * <p>
     * AtomicInteger를 사용하여 경쟁 조건 없이 정확한 세션 수를 반환합니다.
     * </p>
     *
     * @return 활성 세션 수
     */
    public int getActiveSessionCount() {
        return activeSessionCount.get();
    }

    /**
     * SessionConnectEvent에서 sessionId 추출
     */
    private String extractSessionId(SessionConnectEvent event) {
        try {
            return (String) event.getMessage().getHeaders().get("simpSessionId");
        } catch (ClassCastException | NullPointerException e) {
            log.error("SessionConnectEvent에서 sessionId 추출 실패", e);
            return null;
        }
    }
}

package coffeeshout.global.websocket.event;

import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.StompSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionDisconnectEventListener {

    private final StompSessionManager sessionManager;
    private final DelayedPlayerRemovalService delayedPlayerRemovalService;

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        final String sessionId = event.getSessionId();
        final CloseStatus closeStatus = event.getCloseStatus();
        final int totalConnections = sessionManager.getTotalConnectedClientCount();

        log.info("세션 연결 해제 감지: sessionId={}, closeStatus={}, reason={}, 현재 연결된 클라이언트 수={}", 
                sessionId, closeStatus, closeStatus.getReason(), totalConnections);

        // 중복 처리 방지
        if (sessionManager.isDisconnectionProcessed(sessionId)) {
            log.debug("이미 처리된 연결 해제 무시: sessionId={}", sessionId);
            return;
        }

        // 플레이어 세션인지 확인
        if (sessionManager.hasPlayerKey(sessionId)) {
            final String playerKey = sessionManager.getPlayerKey(sessionId);
            log.info("플레이어 세션 해제 감지: playerKey={}, sessionId={}", playerKey, sessionId);

            // 지연 삭제 스케줄링
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, "SESSION_DISCONNECT");
        }
    }
}

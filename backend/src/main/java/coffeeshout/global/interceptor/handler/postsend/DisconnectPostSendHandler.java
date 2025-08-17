package coffeeshout.global.interceptor.handler.postsend;

import coffeeshout.global.interceptor.handler.PostSendHandler;
import coffeeshout.global.metric.WebSocketMetricService;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.StompSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectPostSendHandler implements PostSendHandler {

    private static final String CLIENT_DISCONNECT = "CLIENT_DISCONNECT";

    private final StompSessionManager sessionManager;
    private final WebSocketMetricService webSocketMetricService;
    private final DelayedPlayerRemovalService delayedPlayerRemovalService;

    @Override
    public StompCommand getCommand() {
        return StompCommand.DISCONNECT;
    }

    @Override
    public void handle(StompHeaderAccessor accessor, String sessionId, boolean sent) {
        if (!sent) {
            return;
        }

        if (sessionManager.isDisconnectionProcessed(sessionId)) {
            log.debug("중복 DISCONNECT 무시: sessionId={}", sessionId);
            return;
        }

        if (sessionManager.hasPlayerKey(sessionId)) {
            final String disconnectedPlayerKey = sessionManager.getPlayerKey(sessionId);
            log.info("플레이어 세션 해제: playerKey={}, sessionId={}", disconnectedPlayerKey, sessionId);

            // 지연 삭제 스케줄링
            delayedPlayerRemovalService.schedulePlayerRemoval(disconnectedPlayerKey, sessionId, CLIENT_DISCONNECT);
        }

        // 세션 정리 로직 추가
        log.info("WebSocket 연결 해제 완료: sessionId={}", sessionId);

        webSocketMetricService.recordDisconnection(sessionId, CLIENT_DISCONNECT, true);
    }
}

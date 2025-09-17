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
public class ConnectPostSendHandler implements PostSendHandler {

    private final StompSessionManager sessionManager;
    private final WebSocketMetricService webSocketMetricService;
    private final DelayedPlayerRemovalService delayedPlayerRemovalService;

    @Override
    public StompCommand getCommand() {
        return StompCommand.CONNECT;
    }

    @Override
    public void handle(StompHeaderAccessor accessor, String sessionId, boolean sent) {
        if (sent) {
            // 서버에서 CONNECTED 응답을 성공적으로 보냈을 때 - 이벤트 리스너에서 처리
            return;
        }

        // 연결 응답 실패 - 플레이어 제거
        log.warn("STOMP 메시지 전송 실패: sessionId={}, command={}", sessionId, StompCommand.CONNECTED);

        if (sessionManager.hasPlayerKey(sessionId)) {
            final String failedPlayerKey = sessionManager.getPlayerKey(sessionId);
            sessionManager.removeSession(sessionId);
            delayedPlayerRemovalService.schedulePlayerRemoval(failedPlayerKey, sessionId, "CONNECTION_FAILED");
        }

        webSocketMetricService.failConnection(sessionId, "connection_response_failed");
    }
}

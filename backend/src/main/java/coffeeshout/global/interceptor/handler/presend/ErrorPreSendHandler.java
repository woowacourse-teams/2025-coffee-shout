package coffeeshout.global.interceptor.handler.presend;

import coffeeshout.global.interceptor.handler.PreSendHandler;
import coffeeshout.global.metric.WebSocketMetricService;
import coffeeshout.global.websocket.PlayerDisconnectionService;
import coffeeshout.global.websocket.StompSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorPreSendHandler implements PreSendHandler {

    private final StompSessionManager sessionManager;
    private final WebSocketMetricService webSocketMetricService;
    private final PlayerDisconnectionService playerDisconnectionService;

    @Override
    public StompCommand getCommand() {
        return StompCommand.ERROR;
    }

    @Override
    public void handle(StompHeaderAccessor accessor, String sessionId) {
        final String errorMessage = accessor.getMessage();
        log.error("STOMP 에러: sessionId={}, message={}", sessionId, errorMessage);

        // 에러 발생 시 플레이어 제거
        final String errorPlayerKey = sessionManager.getPlayerKeyBySessionId(sessionId);
        if (errorPlayerKey != null) {
            sessionManager.removeSession(sessionId);
            playerDisconnectionService.handlePlayerDisconnection(errorPlayerKey, sessionId, "STOMP_ERROR");
        }

        webSocketMetricService.recordDisconnection(sessionId, "stomp_error", false);
    }
}
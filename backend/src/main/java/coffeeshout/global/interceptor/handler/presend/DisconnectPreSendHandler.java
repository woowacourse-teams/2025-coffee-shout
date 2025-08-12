package coffeeshout.global.interceptor.handler.presend;

import coffeeshout.global.interceptor.handler.PreSendHandler;
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
public class DisconnectPreSendHandler implements PreSendHandler {

    private final StompSessionManager sessionManager;
    private final PlayerDisconnectionService playerDisconnectionService;

    @Override
    public StompCommand getCommand() {
        return StompCommand.DISCONNECT;
    }

    @Override
    public void handle(StompHeaderAccessor accessor, String sessionId) {
        log.info("WebSocket 연결 해제 요청: sessionId={}", sessionId);
        
        final String disconnectedPlayerKey = sessionManager.getPlayerKeyBySessionId(sessionId);
        if (disconnectedPlayerKey != null) {
            log.info("플레이어 세션 해제: playerKey={}, sessionId={}", disconnectedPlayerKey, sessionId);
            
            // 방에서 플레이어 제거
            playerDisconnectionService.handlePlayerDisconnection(disconnectedPlayerKey, sessionId, "CLIENT_DISCONNECT");
        }
    }
}
package coffeeshout.global.websocket.event;

import coffeeshout.global.metric.WebSocketMetricService;
import coffeeshout.global.websocket.StompSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionConnectEventListener {

    private final WebSocketMetricService webSocketMetricService;
    private final StompSessionManager sessionManager;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        final String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        
        log.info("웹소켓 연결 시작: sessionId={}, joinCode={}, playerName={}", 
                sessionId,
                accessor.getFirstNativeHeader("joinCode"),
                accessor.getFirstNativeHeader("playerName"));
        
        webSocketMetricService.startConnection(sessionId);
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        final String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        
        // 플레이어 정보 가져오기 (있으면)
        String playerInfo = "";
        if (sessionManager.hasPlayerKey(sessionId)) {
            final String playerKey = sessionManager.getPlayerKey(sessionId);
            final String joinCode = sessionManager.extractJoinCode(playerKey);
            final String playerName = sessionManager.extractPlayerName(playerKey);
            playerInfo = String.format(", joinCode=%s, playerName=%s", joinCode, playerName);
        }
        
        log.info("웹소켓 연결 완료: sessionId={}{}", sessionId, playerInfo);
        webSocketMetricService.completeConnection(sessionId);
    }
}

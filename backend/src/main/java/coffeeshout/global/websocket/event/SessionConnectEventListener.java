package coffeeshout.global.websocket.event;

import coffeeshout.global.metric.WebSocketMetricService;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Slf4j
@RequiredArgsConstructor
@Component
public class SessionConnectEventListener {

    private static final String SIMP_SESSION_ID = "simpSessionId";
    private static final String JOIN_CODE = "joinCode";
    private static final String PLAYER_NAME = "playerName";
    private static final String MISSING_HEADERS = "missing_headers";

    private final WebSocketMetricService webSocketMetricService;
    private final StompSessionManager sessionManager;
    private final DelayedPlayerRemovalService delayedPlayerRemovalService;
    private final RoomQueryService roomQueryService;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        final String sessionId = event.getMessage().getHeaders().get(SIMP_SESSION_ID, String.class);
        
        if (sessionId == null) {
            log.error("sessionId가 null입니다");
            return;
        }
        
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            accessor.setSessionAttributes(new ConcurrentHashMap<>());
        }

        final String joinCode = accessor.getFirstNativeHeader(JOIN_CODE);
        final String playerName = accessor.getFirstNativeHeader(PLAYER_NAME);

        log.info("웹소켓 연결 시작: sessionId={}, joinCode={}, playerName={}", sessionId, joinCode, playerName);

        // 헤더 정보 저장 (연결 완료 시 사용)
        if (joinCode == null || playerName == null) {
            log.warn("ConnectEvent 헤더 정보 누락: sessionId={}, joinCode={}, playerName={}", sessionId, joinCode,
                    playerName);
            webSocketMetricService.failConnection(sessionId, MISSING_HEADERS);
            return;
        }

        accessor.getSessionAttributes().put(JOIN_CODE, joinCode);
        accessor.getSessionAttributes().put(PLAYER_NAME, playerName);


        webSocketMetricService.startConnection(sessionId);
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        final String sessionId = event.getMessage().getHeaders().get(SIMP_SESSION_ID, String.class);
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getSessionAttributes() == null) {
            log.warn("SessionConnectedEvent 세션 속성 누락: sessionId={}", sessionId);
            webSocketMetricService.failConnection(sessionId, MISSING_HEADERS);
        }

        String joinCode = (String) accessor.getSessionAttributes().get(JOIN_CODE);
        String playerName = (String) accessor.getSessionAttributes().get(PLAYER_NAME);

        if (joinCode == null || playerName == null) {
            log.warn("ConnectedEvent 헤더 정보 누락: sessionId={}, joinCode={}, playerName={}", sessionId, joinCode,
                    playerName);
            webSocketMetricService.failConnection(sessionId, MISSING_HEADERS);
            return;
        }

        log.info("저장된 정보에서 가져옴: joinCode={}, playerName={}", joinCode, playerName);

        processPlayerConnection(sessionId, joinCode, playerName);

        final String playerInfo = String.format(", joinCode=%s, playerName=%s", joinCode, playerName);
        log.info("웹소켓 연결 완료: sessionId={}{}", sessionId, playerInfo);
        webSocketMetricService.completeConnection(sessionId);
    }

    private void processPlayerConnection(String sessionId, String joinCode, String playerName) {
        // 기존 세션 없으면 첫 연결처리
        if (!sessionManager.hasSessionId(joinCode, playerName)) {
            handlePlayerFirstConnection(sessionId, joinCode, playerName);
            return;
        }

        // 재연결 처리
        final String oldSessionId = sessionManager.getSessionId(joinCode, playerName);
        processPlayerReconnect(sessionId, joinCode, playerName, oldSessionId);
    }

    private void handlePlayerFirstConnection(String sessionId, String joinCode, String playerName) {
        sessionManager.registerPlayerSession(joinCode, playerName, sessionId);
        log.info("플레이어 첫 연결: joinCode={}, playerName={}", joinCode, playerName);
    }

    private void processPlayerReconnect(String sessionId, String joinCode, String playerName, String oldSessionId) {
        log.info("플레이어 재연결 감지: joinCode={}, playerName={}, oldSessionId={}", joinCode, playerName, oldSessionId);

        // 새 세션으로 등록
        sessionManager.registerPlayerSession(joinCode, playerName, sessionId);

        // 기존 지연 삭제 취소
        final String playerKey = sessionManager.createPlayerKey(joinCode, playerName);
        delayedPlayerRemovalService.cancelScheduledRemoval(playerKey);

        // 재연결 처리
        handlePlayerReconnection(joinCode, playerName, sessionId);
    }

    private void handlePlayerReconnection(String joinCode, String playerName, String newSessionId) {
        try {
            // 1. 방 존재 확인
            final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

            // 2. 방 상태 확인
            if (room.isPlayingState()) {
                log.info("게임 중인 방 재연결 거부: joinCode={}, playerName={}", joinCode, playerName);
                sessionManager.removeSession(newSessionId);
                return;
            }

            // 3. READY 상태면 재연결 허용 + 현재 상태 전송
            log.info("방 재연결 허용: joinCode={}, playerName={}", joinCode, playerName);

        } catch (Exception e) {
            log.warn("재연결 실패: joinCode={}, playerName={}, error={}", joinCode, playerName, e.getMessage());
            // 재연결 실패 시 기존 매핑 제거하고 방에서 플레이어 제거
            sessionManager.removeSession(newSessionId);
        }
    }
}

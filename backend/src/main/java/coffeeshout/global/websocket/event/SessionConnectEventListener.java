package coffeeshout.global.websocket.event;

import coffeeshout.global.metric.WebSocketMetricService;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.service.RoomQueryService;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Slf4j
@Component
public class SessionConnectEventListener {

    private final WebSocketMetricService webSocketMetricService;
    private final StompSessionManager sessionManager;
    private final DelayedPlayerRemovalService delayedPlayerRemovalService;
    private final RoomQueryService roomQueryService;

    // 연결 대기 중인 세션 정보 저장
    private final ConcurrentHashMap<String, SessionInfo> pendingConnections;
    private final TaskScheduler cleanupExecutor;

    public SessionConnectEventListener(
            WebSocketMetricService webSocketMetricService,
            StompSessionManager sessionManager,
            DelayedPlayerRemovalService delayedPlayerRemovalService,
            RoomQueryService roomQueryService,
            @Qualifier("delayRemovalScheduler") TaskScheduler cleanupExecutor
    ) {
        this.webSocketMetricService = webSocketMetricService;
        this.sessionManager = sessionManager;
        this.delayedPlayerRemovalService = delayedPlayerRemovalService;
        this.roomQueryService = roomQueryService;
        this.pendingConnections = new ConcurrentHashMap<>();
        this.cleanupExecutor = cleanupExecutor;
    }

    // 세션 정보 저장용 내부 클래스
    @Getter
    private static class SessionInfo {
        private final String joinCode;
        private final String playerName;
        private final long timestamp;

        public SessionInfo(String joinCode, String playerName) {
            this.joinCode = joinCode;
            this.playerName = playerName;
            this.timestamp = System.currentTimeMillis();
        }
    }

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        final String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        final String joinCode = accessor.getFirstNativeHeader("joinCode");
        final String playerName = accessor.getFirstNativeHeader("playerName");

        log.info("웹소켓 연결 시작: sessionId={}, joinCode={}, playerName={}", sessionId, joinCode, playerName);

        // 헤더 정보 저장 (연결 완료 시 사용)
        if (joinCode == null || playerName == null) {
            log.warn("헤더 정보 누락: sessionId={}, joinCode={}, playerName={}", sessionId, joinCode, playerName);
            webSocketMetricService.startConnection(sessionId);
            return;
        }

        pendingConnections.put(sessionId, new SessionInfo(joinCode, playerName));
        // 10초 후 자동 정리 (메모리 누수 방지)
        cleanupExecutor.schedule(() -> {
            SessionInfo removed = pendingConnections.remove(sessionId);
            if (removed != null) {
                log.warn("연결 대기 세션 타임아웃 정리: sessionId={}, joinCode={}, playerName={}",
                        sessionId, removed.getJoinCode(), removed.getPlayerName());
            }
        }, Instant.now().plusSeconds(3));

        webSocketMetricService.startConnection(sessionId);
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        final String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);

        // 저장된 헤더 정보 가져오기
        final SessionInfo sessionInfo = pendingConnections.remove(sessionId);

        if (sessionInfo == null) {
            log.warn("저장된 세션 정보 없음: sessionId={}", sessionId);
            return;
        }

        final String joinCode = sessionInfo.getJoinCode();
        final String playerName = sessionInfo.getPlayerName();
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

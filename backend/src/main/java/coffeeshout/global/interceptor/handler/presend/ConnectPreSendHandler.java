package coffeeshout.global.interceptor.handler.presend;

import coffeeshout.global.interceptor.handler.PreSendHandler;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.service.RoomQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectPreSendHandler implements PreSendHandler {

    private final StompSessionManager sessionManager;
    private final RoomQueryService roomQueryService;
    private final DelayedPlayerRemovalService delayedPlayerRemovalService;

    @Override
    public StompCommand getCommand() {
        return StompCommand.CONNECT;
    }

    @Override
    public void handle(StompHeaderAccessor accessor, String sessionId) {
        final String joinCode = accessor.getFirstNativeHeader("joinCode");
        final String playerName = accessor.getFirstNativeHeader("playerName");

        if (joinCode != null && playerName != null) {
            processPlayerConnection(sessionId, joinCode, playerName);
        }
    }

    private void processPlayerConnection(String sessionId, String joinCode, String playerName) {
        // 기존 세션 있으면 재연결, 없으면 첫 연결
        if (sessionManager.hasSessionId(joinCode, playerName)) {
            String oldSessionId = sessionManager.getSessionId(joinCode, playerName);
            processPlayerReconnect(sessionId, joinCode, playerName, oldSessionId);
            return;
        }

        // 첫 연결 등록 & 처리
        sessionManager.registerPlayerSession(joinCode, playerName, sessionId);

        handlePlayerFirstConnection(joinCode, playerName);
    }

    private void processPlayerReconnect(String sessionId, String joinCode, String playerName, String oldSessionId) {
        log.info("플레이어 재연결 감지: joinCode={}, playerName={}, oldSessionId={}",
                joinCode, playerName, oldSessionId);

        // 새 세션으로 등록
        sessionManager.registerPlayerSession(joinCode, playerName, sessionId);

        // 기존 지연 삭제 취소
        final String playerKey = sessionManager.createPlayerKey(joinCode, playerName);
        delayedPlayerRemovalService.cancelScheduledRemoval(playerKey);

        // 재연결 처리
        handlePlayerReconnection(joinCode, playerName, sessionId);
    }

    private void handlePlayerFirstConnection(String joinCode, String playerName) {
        log.info("플레이어 첫 연결: joinCode={}, playerName={}", joinCode, playerName);
        // TODO: 필요한 경우 첫 연결 시 추가 검증 로직 구현
        // 현재는 REST API에서 방 참여가 이미 완료되었다고 가정
    }

    private void handlePlayerReconnection(String joinCode, String playerName, String newSessionId) {
        try {
            // 1. 방 존재 확인
            final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

            // 2. 방 상태 확인
            if (room.isPlayingState()) {
                log.info("게임 중인 방 재연결 거부: joinCode={}, playerName={}", joinCode, playerName);
                disconnectSession(newSessionId, "GAME_IN_PROGRESS");
                return;
            }

            // 3. READY 상태면 재연결 허용 + 현재 상태 전송
            log.info("방 재연결 허용: joinCode={}, playerName={}", joinCode, playerName);
        } catch (Exception e) {
            log.warn("재연결 실패: joinCode={}, playerName={}, error={}", joinCode, playerName, e.getMessage());
            // 재연결 실패 시 기존 매핑 제거하고 방에서 플레이어 제거
            sessionManager.removeSession(newSessionId);
//            final String playerKey = sessionManager.createPlayerKey(joinCode, playerName);
            // TODO: 플레이어 disconnect 처리 로직 필요 (다른 핸들러에서 처리)
        }
    }

    private void disconnectSession(String sessionId, String reason) {
        log.warn("세션 연결 거부: sessionId={}, reason={}", sessionId, reason);
        // TODO: 필요한 경우 실제 세션 강제 종료 로직 구현
        // 현재는 클라이언트가 연결 실패를 감지하여 자동으로 처리한다고 가정
        sessionManager.removeSession(sessionId);
    }
}

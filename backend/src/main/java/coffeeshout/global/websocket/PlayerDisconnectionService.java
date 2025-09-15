package coffeeshout.global.websocket;

import coffeeshout.global.websocket.event.RoomStateUpdateEvent;
import coffeeshout.room.application.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 플레이어 연결 해제 관련 로직을 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerDisconnectionService {

    private final StompSessionManager sessionManager;
    private final RoomService roomService;
    private final ApplicationEventPublisher eventPublisher;

    public void cancelReady(String playerKey) {
        final String joinCode = sessionManager.extractJoinCode(playerKey);
        final String playerName = sessionManager.extractPlayerName(playerKey);

        roomService.changePlayerReadyState(joinCode, playerName, false);

        eventPublisher.publishEvent(new RoomStateUpdateEvent(joinCode, "PLAYER_SET_READY_FALSE"));
        log.info("삭제 대기된 플레이어 ready 상태 변경 완료: joinCode={}, playerName={}", joinCode, playerName);
    }

    /**
     * 플레이어 연결 해제 처리
     */
    public void handlePlayerDisconnection(String playerKey, String sessionId, String reason) {
        try {
            if (!sessionManager.isValidPlayerKey(playerKey)) {
                log.warn("잘못된 플레이어 키 형식: {}", playerKey);
                return;
            }

            final String joinCode = sessionManager.extractJoinCode(playerKey);
            final String playerName = sessionManager.extractPlayerName(playerKey);

            log.info("플레이어 연결 해제 처리: joinCode={}, playerName={}, reason={}", joinCode, playerName, reason);

        } catch (Exception e) {
            log.error("플레이어 연결 해제 처리 실패: playerKey={}, sessionId={}, reason={}", playerKey, sessionId, reason, e);
        }
    }
}

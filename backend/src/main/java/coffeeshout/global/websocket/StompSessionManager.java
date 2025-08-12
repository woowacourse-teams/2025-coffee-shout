package coffeeshout.global.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class StompSessionManager {

    private static final String PLAYER_KEY_DELIMITER = ":";
    private static final int EXPECTED_PLAYER_KEY_PARTS = 2;

    // 중복 처리 방지용
    private final Set<String> processedDisconnections = ConcurrentHashMap.newKeySet();

    // 플레이어 세션 매핑 관리
    private final ConcurrentHashMap<String, String> playerSessionMap; // "joinCode:playerName" -> sessionId
    private final ConcurrentHashMap<String, String> sessionPlayerMap; // sessionId -> "joinCode:playerName"

    public StompSessionManager() {
        playerSessionMap = new ConcurrentHashMap<>();
        sessionPlayerMap = new ConcurrentHashMap<>();
    }

    /**
     * 플레이어 세션 매핑 등록
     */
    public void registerPlayerSession(String joinCode, String playerName, String sessionId) {
        String playerKey = createPlayerKey(joinCode, playerName);

        // 기존 세션이 있으면 정리
        String oldSessionId = playerSessionMap.get(playerKey);
        if (oldSessionId != null) {
            log.info("기존 플레이어 세션 정리: playerKey={}, oldSessionId={}", playerKey, oldSessionId);
            sessionPlayerMap.remove(oldSessionId);
        }

        playerSessionMap.put(playerKey, sessionId);
        sessionPlayerMap.put(sessionId, playerKey);
        log.info("플레이어 세션 매핑 등록: playerKey={}, sessionId={}", playerKey, sessionId);
    }

    /**
     * 플레이어의 기존 세션 ID 조회
     */
    public String getExistingSessionId(String joinCode, String playerName) {
        String playerKey = createPlayerKey(joinCode, playerName);
        return playerSessionMap.get(playerKey);
    }

    /**
     * 세션 ID로 플레이어 키 조회
     */
    public String getPlayerKeyBySessionId(String sessionId) {
        return sessionPlayerMap.get(sessionId);
    }

    /**
     * 플레이어 키에서 조인 코드 추출
     */
    public String extractJoinCode(String playerKey) {
        validatePlayerKey(playerKey);
        String[] parts = playerKey.split(PLAYER_KEY_DELIMITER);
        return parts[0];
    }

    /**
     * 플레이어 키에서 플레이어 이름 추출
     */
    public String extractPlayerName(String playerKey) {
        validatePlayerKey(playerKey);
        String[] parts = playerKey.split(PLAYER_KEY_DELIMITER);
        return parts[1];
    }

    /**
     * 세션 매핑 제거
     */
    public void removeSession(String sessionId) {
        String playerKey = sessionPlayerMap.remove(sessionId);
        if (playerKey != null) {
            playerSessionMap.remove(playerKey);
            log.info("세션 매핑 제거: playerKey={}, sessionId={}", playerKey, sessionId);
        }
    }

    /**
     * 플레이어 세션 제거
     */
    public void removePlayerSession(String joinCode, String playerName) {
        String playerKey = createPlayerKey(joinCode, playerName);
        String sessionId = playerSessionMap.remove(playerKey);
        if (sessionId != null) {
            sessionPlayerMap.remove(sessionId);
            log.info("플레이어 세션 제거: playerKey={}, sessionId={}", playerKey, sessionId);
        }
    }

    /**
     * 중복 disconnection 처리 방지를 위한 체크 및 등록
     */
    public boolean isDisconnectionProcessed(String sessionId) {
        return !processedDisconnections.add(sessionId);
    }

    /**
     * 중복 disconnection 처리 상태 제거
     */
    public void removeDisconnectionProcessed(String sessionId) {
        processedDisconnections.remove(sessionId);
    }

    /**
     * 현재 연결된 세션 수 조회
     */
    public int getConnectedSessionCount() {
        return sessionPlayerMap.size();
    }

    /**
     * 특정 방의 연결된 플레이어 수 조회
     */
    public long getConnectedPlayerCountByJoinCode(String joinCode) {
        return playerSessionMap.keySet().stream()
                .filter(playerKey -> playerKey.startsWith(joinCode + PLAYER_KEY_DELIMITER))
                .count();
    }

    /**
     * 플레이어 키 생성 (public 메서드)
     */
    public String createPlayerKey(String joinCode, String playerName) {
        if (joinCode == null || playerName == null) {
            throw new IllegalArgumentException("joinCode와 playerName은 null일 수 없습니다");
        }
        if (joinCode.contains(PLAYER_KEY_DELIMITER) || playerName.contains(PLAYER_KEY_DELIMITER)) {
            throw new IllegalArgumentException("joinCode와 playerName에 구분자('" + PLAYER_KEY_DELIMITER + "')가 포함될 수 없습니다");
        }
        return joinCode + PLAYER_KEY_DELIMITER + playerName;
    }

    /**
     * 플레이어 키 유효성 검증
     */
    public boolean isValidPlayerKey(String playerKey) {
        if (playerKey == null || !playerKey.contains(PLAYER_KEY_DELIMITER)) {
            return false;
        }
        String[] parts = playerKey.split(PLAYER_KEY_DELIMITER);
        return parts.length == EXPECTED_PLAYER_KEY_PARTS &&
               !parts[0].isEmpty() && !parts[1].isEmpty();
    }

    /**
     * 플레이어 키 유효성 검증 및 예외 발생
     */
    private void validatePlayerKey(String playerKey) {
        if (playerKey == null) {
            throw new IllegalArgumentException("플레이어 키가 null입니다");
        }
        if (!playerKey.contains(PLAYER_KEY_DELIMITER)) {
            throw new IllegalArgumentException("플레이어 키에 구분자('" + PLAYER_KEY_DELIMITER + "')가 없습니다: " + playerKey);
        }
        String[] parts = playerKey.split(PLAYER_KEY_DELIMITER);
        if (parts.length != EXPECTED_PLAYER_KEY_PARTS) {
            throw new IllegalArgumentException("플레이어 키 형식이 잘못되었습니다. 예상: joinCode" + PLAYER_KEY_DELIMITER + "playerName, 실제: " + playerKey);
        }
        if (parts[0].isEmpty() || parts[1].isEmpty()) {
            throw new IllegalArgumentException("joinCode 또는 playerName이 비어있습니다: " + playerKey);
        }
    }
}

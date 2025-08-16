package coffeeshout.global.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StompSessionManagerTest {

    private StompSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new StompSessionManager();
    }

    @Test
    @DisplayName("정상적인 플레이어 키 생성")
    void createPlayerKey_Success() {
        // given
        String joinCode = "ABC23";
        String playerName = "player1";

        // when
        String playerKey = sessionManager.createPlayerKey(joinCode, playerName);

        // then
        assertThat(playerKey).isEqualTo("ABC23:player1");
    }

    @Test
    @DisplayName("joinCode가 null인 경우 예외 발생")
    void createPlayerKey_NullJoinCode_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> sessionManager.createPlayerKey(null, "player1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("joinCode와 playerName은 null일 수 없습니다");
    }

    @Test
    @DisplayName("playerName이 null인 경우 예외 발생")
    void createPlayerKey_NullPlayerName_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> sessionManager.createPlayerKey("ABC23", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("joinCode와 playerName은 null일 수 없습니다");
    }

    @Test
    @DisplayName("joinCode에 구분자가 포함된 경우 예외 발생")
    void createPlayerKey_JoinCodeContainsDelimiter_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> sessionManager.createPlayerKey("ABC:23", "player1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("joinCode와 playerName에 구분자(':')가 포함될 수 없습니다");
    }

    @Test
    @DisplayName("playerName에 구분자가 포함된 경우 예외 발생")
    void createPlayerKey_PlayerNameContainsDelimiter_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> sessionManager.createPlayerKey("ABC23", "play:er1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("joinCode와 playerName에 구분자(':')가 포함될 수 없습니다");
    }

    @Test
    @DisplayName("정상적인 joinCode 추출")
    void extractJoinCode_Success() {
        // given
        String playerKey = "ABC23:player1";

        // when
        String joinCode = sessionManager.extractJoinCode(playerKey);

        // then
        assertThat(joinCode).isEqualTo("ABC23");
    }

    @Test
    @DisplayName("정상적인 playerName 추출")
    void extractPlayerName_Success() {
        // given
        String playerKey = "ABC23:player1";

        // when
        String playerName = sessionManager.extractPlayerName(playerKey);

        // then
        assertThat(playerName).isEqualTo("player1");
    }

    @Test
    @DisplayName("null 플레이어 키로 joinCode 추출 시 예외 발생")
    void extractJoinCode_NullPlayerKey_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> sessionManager.extractJoinCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("플레이어 키가 null입니다");
    }

    @Test
    @DisplayName("구분자가 없는 플레이어 키로 joinCode 추출 시 예외 발생")
    void extractJoinCode_NoDelimiter_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> sessionManager.extractJoinCode("ABC23player1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("플레이어 키에 구분자(':')가 없습니다: ABC23player1");
    }

    @Test
    @DisplayName("잘못된 형식의 플레이어 키로 playerName 추출 시 예외 발생")
    void extractPlayerName_InvalidFormat_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> sessionManager.extractPlayerName("ABC23:player1:extra"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("플레이어 키 형식이 잘못되었습니다. 예상: joinCode:playerName, 실제: ABC23:player1:extra");
    }

    @Test
    @DisplayName("빈 joinCode가 있는 플레이어 키 검증 시 예외 발생")
    void extractJoinCode_EmptyJoinCode_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> sessionManager.extractJoinCode(":player1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("joinCode 또는 playerName이 비어있습니다: :player1");
    }

    @Test
    @DisplayName("플레이어 키 유효성 검증 - 정상")
    void isValidPlayerKey_ValidKey_ReturnsTrue() {
        // given
        String validPlayerKey = "ABC23:player1";

        // when & then
        assertThat(sessionManager.isValidPlayerKey(validPlayerKey)).isTrue();
    }

    @Test
    @DisplayName("플레이어 키 유효성 검증 - null")
    void isValidPlayerKey_NullKey_ReturnsFalse() {
        // when & then
        assertThat(sessionManager.isValidPlayerKey(null)).isFalse();
    }

    @Test
    @DisplayName("플레이어 키 유효성 검증 - 구분자 없음")
    void isValidPlayerKey_NoDelimiter_ReturnsFalse() {
        // when & then
        assertThat(sessionManager.isValidPlayerKey("ABC23player1")).isFalse();
    }

    @Test
    @DisplayName("플레이어 키 유효성 검증 - 빈 joinCode")
    void isValidPlayerKey_EmptyJoinCode_ReturnsFalse() {
        // when & then
        assertThat(sessionManager.isValidPlayerKey(":player1")).isFalse();
    }

    @Test
    @DisplayName("플레이어 세션 등록 및 조회")
    void registerAndGetPlayerSession() {
        // given
        String joinCode = "ABC23";
        String playerName = "player1";
        String sessionId = "session123";

        // when
        sessionManager.registerPlayerSession(joinCode, playerName, sessionId);

        // then
        assertThat(sessionManager.getSessionId(joinCode, playerName)).isEqualTo(sessionId);
        assertThat(sessionManager.getPlayerKey(sessionId)).isEqualTo("ABC23:player1");
    }

    @Test
    @DisplayName("특정 방의 연결된 플레이어 수 조회")
    void getConnectedPlayerCountByJoinCode() {
        // given
        String joinCode = "ABC23";
        sessionManager.registerPlayerSession(joinCode, "player1", "session1");
        sessionManager.registerPlayerSession(joinCode, "player2", "session2");
        sessionManager.registerPlayerSession("XYZ789", "player3", "session3");

        // when
        long count = sessionManager.getConnectedPlayerCountByJoinCode(joinCode);

        // then
        assertThat(count).isEqualTo(2);
    }
}

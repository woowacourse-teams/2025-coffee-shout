package coffeeshout.global.interceptor.handler.postsend;

import coffeeshout.global.metric.WebSocketMetricService;
import coffeeshout.global.websocket.PlayerDisconnectionService;
import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.room.application.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisconnectPostSendHandlerTest {

    @Mock
    private WebSocketMetricService webSocketMetricService;

    @Mock
    private RoomService roomService;

    private StompSessionManager sessionManager;
    private DisconnectPostSendHandler handler;

    private final String sessionId = "test-session-id";
    private final String joinCode = "TEST123";
    private final String playerName = "testPlayer";

    @BeforeEach
    void setUp() {
        sessionManager = new StompSessionManager();
        final PlayerDisconnectionService playerDisconnectionService = new PlayerDisconnectionService(sessionManager,
                roomService);
        handler = new DisconnectPostSendHandler(sessionManager, webSocketMetricService, playerDisconnectionService);
    }

    @Test
    void 핸들러가_DISCONNECT_커맨드를_처리한다() {
        // when & then
        assertThat(handler.getCommand()).isEqualTo(StompCommand.DISCONNECT);
    }

    @Nested
    class 연결_해제_실패_시나리오 {

        @Test
        void 전송_실패_시_처리하지_않는다() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);

            // when
            handler.handle(accessor, sessionId, false);

            // then
            verifyNoInteractions(webSocketMetricService);
            verifyNoInteractions(roomService);
        }

        @Test
        void 이미_처리된_세션의_중복_DISCONNECT를_무시한다() {
            // given
            sessionManager.registerPlayerSession(joinCode, playerName, sessionId);
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);
            
            // 이미 처리된 상태로 설정
            sessionManager.isDisconnectionProcessed(sessionId);

            // when
            handler.handle(accessor, sessionId, true);

            // then
            // 세션이 여전히 존재하는지 확인 (중복 처리로 무시됨)
            assertThat(sessionManager.getPlayerKeyBySessionId(sessionId)).isEqualTo(joinCode + ":" + playerName);
            verifyNoInteractions(webSocketMetricService);
            verifyNoInteractions(roomService);
        }
    }

    @Nested
    class 일반_세션_해제 {

        @Test
        void 플레이어_세션이_없는_일반_세션_해제를_처리한다() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);

            // when
            handler.handle(accessor, sessionId, true);

            // then
            then(webSocketMetricService).should().recordDisconnection(sessionId, "CLIENT_DISCONNECT", true);
            assertThat(sessionManager.getPlayerKeyBySessionId(sessionId)).isNull();
            verifyNoInteractions(roomService);
        }
    }

    @Nested
    class 플레이어_세션_해제 {

        @Test
        void 플레이어_세션_해제를_정상적으로_처리한다() {
            // given
            sessionManager.registerPlayerSession(joinCode, playerName, sessionId);
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);

            // when
            handler.handle(accessor, sessionId, true);

            // then
            assertThat(sessionManager.getPlayerKeyBySessionId(sessionId)).isNull();
            then(webSocketMetricService).should().recordDisconnection(sessionId, "CLIENT_DISCONNECT", true);
        }

        @Test
        void 세션_매핑이_완전히_제거된다() {
            // given
            sessionManager.registerPlayerSession(joinCode, playerName, sessionId);
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);
            
            // 초기 상태 확인
            assertThat(sessionManager.getPlayerKeyBySessionId(sessionId)).isNotNull();
            assertThat(sessionManager.getExistingSessionId(joinCode, playerName)).isEqualTo(sessionId);

            // when
            handler.handle(accessor, sessionId, true);

            // then
            assertThat(sessionManager.getPlayerKeyBySessionId(sessionId)).isNull();
            assertThat(sessionManager.getExistingSessionId(joinCode, playerName)).isNull();
        }

        @Test
        void 플레이어_연결_해제_서비스가_호출된다() {
            // given
            sessionManager.registerPlayerSession(joinCode, playerName, sessionId);
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);

            // when
            handler.handle(accessor, sessionId, true);

            // then
            then(roomService).should().removePlayer(joinCode, playerName);
        }
    }

    @Nested
    class 메트릭_처리 {

        @Test
        void 모든_성공적인_연결_해제에서_메트릭이_기록된다() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);

            // when
            handler.handle(accessor, sessionId, true);

            // then
            then(webSocketMetricService).should().recordDisconnection(sessionId, "CLIENT_DISCONNECT", true);
        }

        @Test
        void 플레이어_세션_해제_시에도_메트릭이_기록된다() {
            // given
            sessionManager.registerPlayerSession(joinCode, playerName, sessionId);
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);

            // when
            handler.handle(accessor, sessionId, true);

            // then
            then(webSocketMetricService).should().recordDisconnection(sessionId, "CLIENT_DISCONNECT", true);
        }
    }

    @Nested
    class 세션_카운트_검증 {

        @Test
        void 연결_해제_후_세션_카운트가_감소한다() {
            // given
            sessionManager.registerPlayerSession(joinCode, playerName, sessionId);
            String anotherSessionId = "another-session";
            sessionManager.registerPlayerSession("ROOM456", "anotherPlayer", anotherSessionId);
            
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);
            
            // 초기 상태 확인
            assertThat(sessionManager.getConnectedPlayerCountByJoinCode(joinCode)).isEqualTo(1);

            // when
            handler.handle(accessor, sessionId, true);

            // then
            assertThat(sessionManager.getConnectedPlayerCountByJoinCode(joinCode)).isZero();
        }
    }
}
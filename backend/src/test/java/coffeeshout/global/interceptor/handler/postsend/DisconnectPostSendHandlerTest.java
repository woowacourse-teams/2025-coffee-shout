package coffeeshout.global.interceptor.handler.postsend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import coffeeshout.global.metric.WebSocketMetricService;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.StompSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

@ExtendWith(MockitoExtension.class)
class DisconnectPostSendHandlerTest {

    @Mock
    WebSocketMetricService webSocketMetricService;

    @Mock
    DelayedPlayerRemovalService delayedPlayerRemovalService;

    StompSessionManager sessionManager;
    DisconnectPostSendHandler handler;

    final String sessionId = "test-session-id";
    final String joinCode = "TEST123";
    final String playerName = "testPlayer";

    @BeforeEach
    void setUp() {
        sessionManager = new StompSessionManager();

        handler = new DisconnectPostSendHandler(webSocketMetricService);
    }

    @Test
    void 핸들러가_DISCONNECT_커맨드를_처리한다() {
        // when & then
        assertThat(handler.getCommand()).isEqualTo(StompCommand.DISCONNECT);
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
}

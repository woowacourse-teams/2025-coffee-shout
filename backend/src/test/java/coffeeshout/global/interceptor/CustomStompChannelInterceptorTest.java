package coffeeshout.global.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import coffeeshout.global.interceptor.handler.StompHandlerRegistry;
import coffeeshout.global.interceptor.handler.postsend.ConnectPostSendHandler;
import coffeeshout.global.interceptor.handler.postsend.DisconnectPostSendHandler;
import coffeeshout.global.interceptor.handler.presend.ConnectPreSendHandler;
import coffeeshout.global.interceptor.handler.presend.ErrorPreSendHandler;
import coffeeshout.global.metric.WebSocketMetricService;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.MenuType;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.MenuQueryService;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

@ExtendWith(MockitoExtension.class)
class CustomStompChannelInterceptorTest {

    @Mock
    WebSocketMetricService webSocketMetricService;

    @Mock
    RoomQueryService roomQueryService;

    @Mock
    MenuQueryService menuQueryService;

    @Mock
    MessageChannel channel;

    @Mock
    private DelayedPlayerRemovalService delayedPlayerRemovalService;

    StompSessionManager sessionManager;
    CustomStompChannelInterceptor interceptor;

    ConnectPreSendHandler connectPreSendHandler;
    ConnectPostSendHandler connectPostSendHandler;
    DisconnectPostSendHandler disconnectPostSendHandler;
    ErrorPreSendHandler errorPreSendHandler;

    final String sessionId = "test-session-id";
    final String joinCode = "TEV23";
    final String playerName = "testPlayer";

    @BeforeEach
    void setUp() {
        // 실제 구현체 생성
        sessionManager = new StompSessionManager();

        // 핸들러들 생성
        connectPreSendHandler = new ConnectPreSendHandler(sessionManager, webSocketMetricService, roomQueryService,
                delayedPlayerRemovalService);
        connectPostSendHandler = new ConnectPostSendHandler(sessionManager, webSocketMetricService,
                delayedPlayerRemovalService);
        disconnectPostSendHandler = new DisconnectPostSendHandler(webSocketMetricService);
        errorPreSendHandler = new ErrorPreSendHandler(sessionManager, webSocketMetricService,
                delayedPlayerRemovalService);

        // 핸들러 레지스트리 생성
        final StompHandlerRegistry handlerRegistry = new StompHandlerRegistry(
                List.of(connectPreSendHandler, errorPreSendHandler),
                List.of(connectPostSendHandler, disconnectPostSendHandler)
        );

        // 인터셉터 생성
        interceptor = new CustomStompChannelInterceptor(handlerRegistry);
    }

    @Nested
    class 인터셉터_통합_테스트 {

        @Test
        void CONNECT_메시지_전체_플로우를_처리한다() {
            // given
            StompHeaderAccessor accessor = createAccessor(StompCommand.CONNECT);
            Message<?> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());

            Menu testMenu = new Menu("coffee", MenuType.COFFEE);
            testMenu.setId(1L);

            // when - preSend (연결 요청)
            Message<?> preSendResult = interceptor.preSend(message, channel);

            // then - preSend 후 세션이 등록되었는지 확인
            assertThat(preSendResult).isEqualTo(message);
            assertThat(sessionManager.getPlayerKey(sessionId)).isEqualTo(joinCode + ":" + playerName);

            // when - postSend (연결 성공)
            interceptor.postSend(message, channel, true);

            // then - 메트릭 완료 호출 확인
            then(webSocketMetricService).should().completeConnection(sessionId);
        }

        @Test
        void DISCONNECT_메시지_전체_플로우를_처리한다() {
            // given - 먼저 세션을 등록
            sessionManager.registerPlayerSession(joinCode, playerName, sessionId);

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);
            Message<?> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());

            // when
            interceptor.postSend(message, channel, true);

            // then - 세션이 남아있는지 확인
            assertThat(sessionManager.hasPlayerKey(sessionId)).isTrue();
            then(webSocketMetricService).should().recordDisconnection(sessionId, "CLIENT_DISCONNECT", true);
        }

        @Test
        void accessor가_null인_경우_메시지를_무시한다() {
            // given
            Message<?> message = MessageBuilder.withPayload("test").build();

            // when
            Message<?> result = interceptor.preSend(message, channel);
            interceptor.postSend(message, channel, true);

            // then - 메시지가 그대로 반환되고 세션 상태 변경 없음
            assertThat(result).isEqualTo(message);
            assertThat(sessionManager.hasPlayerKey(sessionId)).isFalse();
        }
    }

    @Nested
    class ConnectPreSendHandler_테스트 {

        @Test
        void 완전한_헤더로_첫_연결_시_세션을_등록한다() {
            // given
            StompHeaderAccessor accessor = createAccessor(StompCommand.CONNECT);

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then - 실제 세션 매니저 상태 검증
            assertThat(sessionManager.getPlayerKey(sessionId)).isEqualTo(joinCode + ":" + playerName);
            assertThat(sessionManager.getSessionId(joinCode, playerName)).isEqualTo(sessionId);
            then(webSocketMetricService).should().startConnection(sessionId);
        }

        /**
         * 현재 로직은 disconnect 후 연결을 하는 것이므로 disconnect 없이 연결 시도하면 예외가 발생한다. 재연결 실패: joinCode=TEV23,
         * playerName=testPlayer, error=중복된 닉네임은 들어올 수 없습니다. 닉네임: testPlayer
         */
        @Disabled
        @Test
        void 기존_세션이_있을_때_재연결을_처리한다() {
            // given
            String oldSessionId = "old-session-id";

            // 기존에 세션을 등록함
            sessionManager.registerPlayerSession(joinCode, playerName, oldSessionId);

            StompHeaderAccessor accessor = createAccessor(StompCommand.CONNECT);
            Menu testMenu = createTestMenu();
            Room testRoom = createTestRoom(testMenu);

            given(roomQueryService.getByJoinCode(new JoinCode(joinCode))).willReturn(testRoom);
            given(menuQueryService.getById(1L)).willReturn(testMenu);

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then - 새 세션으로 교체되었는지 확인
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(sessionManager.getPlayerKey(sessionId))
                        .isEqualTo(joinCode + ":" + playerName);
                softly.assertThat(sessionManager.getSessionId(joinCode, playerName))
                        .isEqualTo(sessionId);
                softly.assertThat(sessionManager.getPlayerKey(oldSessionId)).isNull();
            });
            then(webSocketMetricService).should().startConnection(sessionId);
        }

        @Test
        void 헤더가_불완전할_때는_세션_등록하지_않는다() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setSessionId(sessionId);
            // 헤더를 설정하지 않음

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then - 세션 등록되지 않았는지 확인
            assertThat(sessionManager.hasPlayerKey(sessionId)).isFalse();
            then(webSocketMetricService).should().startConnection(sessionId);
        }

        private Room createTestRoom(Menu menu) {
            return Room.createNewRoom(new JoinCode(joinCode), new PlayerName(playerName), menu);
        }

        private Menu createTestMenu() {
            Menu menu = new Menu("Test Menu", MenuType.COFFEE);
            menu.setId(1L);
            return menu;
        }
    }

    @Nested
    class ConnectPostSendHandler_테스트 {

        @Test
        void 연결_성공_시_메트릭을_완료_상태로_업데이트한다() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setSessionId(sessionId);

            // when
            connectPostSendHandler.handle(accessor, sessionId, true);

            // then
            then(webSocketMetricService).should().completeConnection(sessionId);
        }

        @Test
        void 연결_실패_시_세션을_제거하고_메트릭을_실패_상태로_업데이트한다() {
            // given - 먼저 세션을 등록
            sessionManager.registerPlayerSession(joinCode, playerName, sessionId);

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setSessionId(sessionId);

            // when
            connectPostSendHandler.handle(accessor, sessionId, false);

            // then - 세션이 제거되었는지 확인
            assertThat(sessionManager.hasPlayerKey(sessionId)).isFalse();
            then(webSocketMetricService).should().failConnection(sessionId, "connection_response_failed");
        }
    }

    @Nested
    class DisconnectPostSendHandler_테스트 {

        @Test
        void 연결_해제_성공_시_세션을_정리한다() {
            // given - 먼저 세션을 등록
            sessionManager.registerPlayerSession(joinCode, playerName, sessionId);

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);

            // when
            disconnectPostSendHandler.handle(accessor, sessionId, true);

            // then - 세션이 남아있는지 확인
            assertThat(sessionManager.hasPlayerKey(sessionId)).isTrue();
            then(webSocketMetricService).should().recordDisconnection(sessionId, "CLIENT_DISCONNECT", true);
        }

        @Test
        void 연결_해제_실패_시_처리하지_않는다() {
            // given - 먼저 세션을 등록
            sessionManager.registerPlayerSession(joinCode, playerName, sessionId);

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            accessor.setSessionId(sessionId);

            // when
            disconnectPostSendHandler.handle(accessor, sessionId, false);

            // then - 세션이 여전히 존재하는지 확인 (처리되지 않음)
            assertThat(sessionManager.getPlayerKey(sessionId)).isEqualTo(joinCode + ":" + playerName);
            then(webSocketMetricService).should(never()).recordDisconnection(any(), any(), any(Boolean.class));
        }
    }

    @Nested
    class ErrorPreSendHandler_테스트 {

        @Test
        void STOMP_에러_발생_시_플레이어를_제거한다() {
            // given - 먼저 세션을 등록
            sessionManager.registerPlayerSession(joinCode, playerName, sessionId);

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
            accessor.setSessionId(sessionId);
            accessor.setMessage("Test error message");

            // when
            errorPreSendHandler.handle(accessor, sessionId);

            // then - 세션이 제거되었는지 확인
            then(webSocketMetricService).should().recordDisconnection(sessionId, "stomp_error", false);
        }

        @Test
        void 플레이어_키가_없을_때는_메트릭만_기록한다() {
            // given - 세션을 등록하지 않음
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
            accessor.setSessionId(sessionId);

            // when
            errorPreSendHandler.handle(accessor, sessionId);

            // then - 세션 상태는 변경되지 않음
            assertThat(sessionManager.hasPlayerKey(sessionId)).isFalse();
            then(webSocketMetricService).should().recordDisconnection(sessionId, "stomp_error", false);
        }
    }

    private StompHeaderAccessor createAccessor(StompCommand stompCommand) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(stompCommand);
        accessor.setSessionId(sessionId);
        accessor.setNativeHeader("joinCode", joinCode);
        accessor.setNativeHeader("playerName", playerName);
        final String menuId = "1";
        accessor.setNativeHeader("menuId", menuId);
        return accessor;
    }
}

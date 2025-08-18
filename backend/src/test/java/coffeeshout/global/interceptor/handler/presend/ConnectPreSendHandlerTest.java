package coffeeshout.global.interceptor.handler.presend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import coffeeshout.global.exception.GlobalErrorCode;
import coffeeshout.global.exception.custom.NotExistElementException;
import coffeeshout.global.metric.WebSocketMetricService;
import coffeeshout.global.websocket.DelayedPlayerRemovalService;
import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.MenuType;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConnectPreSendHandlerTest {

    @Mock
    private WebSocketMetricService webSocketMetricService;

    @Mock
    private RoomQueryService roomQueryService;

    @Mock
    private DelayedPlayerRemovalService delayedPlayerRemovalService;

    private StompSessionManager sessionManager;
    private ConnectPreSendHandler connectPreSendHandler;

    private final String sessionId = "test-session-id";
    private final String joinCode = "TES23";
    private final String playerName = "testPlayer";
    private final String menuId = "1";

    @BeforeEach
    void setUp() {
        sessionManager = new StompSessionManager();
        connectPreSendHandler = new ConnectPreSendHandler(
                sessionManager,
                webSocketMetricService,
                roomQueryService,
                delayedPlayerRemovalService
        );
    }

    @Test
    void 핸들러가_CONNECT_커맨드를_처리한다() {
        // when & then
        assertThat(connectPreSendHandler.getCommand()).isEqualTo(StompCommand.CONNECT);
    }

    @Nested
    class 첫_연결_시나리오 {

        @Test
        void 완전한_헤더로_첫_연결_시_세션을_등록한다() {
            // given
            StompHeaderAccessor accessor = createConnectAccessor();

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then
            assertThat(sessionManager.getPlayerKey(sessionId))
                    .isEqualTo(joinCode + ":" + playerName);
            assertThat(sessionManager.getSessionId(joinCode, playerName))
                    .isEqualTo(sessionId);
            then(webSocketMetricService).should().startConnection(sessionId);
        }

        @Test
        void 헤더가_불완전할_때는_세션_등록하지_않는다() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setSessionId(sessionId);
            // joinCode, playerName, menuId 헤더를 설정하지 않음

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then
            assertThat(sessionManager.hasPlayerKey(sessionId)).isFalse();
            then(webSocketMetricService).should().startConnection(sessionId);
        }

        @Test
        void joinCode가_없으면_세션_등록하지_않는다() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setSessionId(sessionId);
            accessor.setNativeHeader("playerName", playerName);
            accessor.setNativeHeader("menuId", menuId);
            // joinCode 헤더 누락

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then
            assertThat(sessionManager.hasPlayerKey(sessionId)).isFalse();
            then(webSocketMetricService).should().startConnection(sessionId);
        }

        @Test
        void playerName이_없으면_세션_등록하지_않는다() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setSessionId(sessionId);
            accessor.setNativeHeader("joinCode", joinCode);
            accessor.setNativeHeader("menuId", menuId);
            // playerName 헤더 누락

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then
            assertThat(sessionManager.hasPlayerKey(sessionId)).isFalse();
            then(webSocketMetricService).should().startConnection(sessionId);
        }
    }

    @Nested
    class 재연결_시나리오 {

        @Test
        void 게임_중인_방에_재연결_시도하면_세션이_제거된다() {
            // given
            String oldSessionId = "old-session-id";
            sessionManager.registerPlayerSession(joinCode, playerName, oldSessionId);

            StompHeaderAccessor accessor = createConnectAccessor();

            Menu testMenu = createTestMenu();
            Room testRoom = createPlayingRoom(testMenu);

            given(roomQueryService.getByJoinCode(any(JoinCode.class))).willReturn(testRoom);

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then
            assertThat(sessionManager.hasPlayerKey(sessionId)).isFalse();
            then(webSocketMetricService).should().startConnection(sessionId);
        }

        @Test
        void 재연결_중_방을_찾을_수_없으면_세션이_제거된다() {
            // given
            String oldSessionId = "old-session-id";
            sessionManager.registerPlayerSession(joinCode, playerName, oldSessionId);

            StompHeaderAccessor accessor = createConnectAccessor();

            given(roomQueryService.getByJoinCode(any(JoinCode.class)))
                    .willThrow(new NotExistElementException(GlobalErrorCode.NOT_EXIST, "방이 존재하지 않습니다."));

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then
            assertThat(sessionManager.hasPlayerKey(sessionId)).isFalse();
            then(webSocketMetricService).should().startConnection(sessionId);
        }

        @Test
        void 잘못된_menuId로_재연결_시도하면_세션이_제거된다() {
            // given
            String oldSessionId = "old-session-id";
            sessionManager.registerPlayerSession(joinCode, playerName, oldSessionId);

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setSessionId(sessionId);
            accessor.setNativeHeader("joinCode", joinCode);
            accessor.setNativeHeader("playerName", playerName);
            accessor.setNativeHeader("menuId", "invalid-menu-id"); // 잘못된 menuId

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then
            assertThat(sessionManager.hasPlayerKey(sessionId)).isFalse();
            then(webSocketMetricService).should().startConnection(sessionId);
        }
    }

    @Nested
    class 메트릭_처리 {

        @Test
        void 모든_경우에_메트릭_시작이_호출된다() {
            // given
            StompHeaderAccessor accessor = createConnectAccessor();

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then
            then(webSocketMetricService).should().startConnection(sessionId);
        }

        @Test
        void 헤더가_없어도_메트릭_시작이_호출된다() {
            // given
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setSessionId(sessionId);

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then
            then(webSocketMetricService).should().startConnection(sessionId);
        }
    }

    @Nested
    class 세션_관리 {

        @Test
        void 세션_매니저_상태가_정확히_업데이트된다() {
            // given
            StompHeaderAccessor accessor = createConnectAccessor();

            // when
            connectPreSendHandler.handle(accessor, sessionId);

            // then
            assertThat(sessionManager.getConnectedPlayerCountByJoinCode(joinCode)).isEqualTo(1);
        }
    }

    private StompHeaderAccessor createConnectAccessor() {
        return createConnectAccessorWithSessionId(sessionId);
    }

    private StompHeaderAccessor createConnectAccessorWithSessionId(String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionId(sessionId);
        accessor.setNativeHeader("joinCode", joinCode);
        accessor.setNativeHeader("playerName", playerName);
        accessor.setNativeHeader("menuId", menuId);
        return accessor;
    }

    private Menu createTestMenu() {
        Menu menu = new Menu("Test Menu", MenuType.COFFEE);
        menu.setId(1L);
        return menu;
    }

    private Room createPlayingRoom(Menu menu) {
        Room room = Room.createNewRoom(new JoinCode(joinCode), new PlayerName(playerName), menu);
        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);

        return room;
    }
}

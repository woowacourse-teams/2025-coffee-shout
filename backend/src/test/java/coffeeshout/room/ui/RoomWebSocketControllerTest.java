//package coffeeshout.room.ui;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import coffeeshout.fixture.TypeReferenceFrameHandler;
//import coffeeshout.fixture.WebSocketIntegrationTestSupport;
//import coffeeshout.global.ui.WebSocketResponse;
//import coffeeshout.room.domain.JoinCode;
//import coffeeshout.room.domain.Room;
//import coffeeshout.room.domain.player.Menu;
//import coffeeshout.room.domain.player.PlayerName;
//import coffeeshout.room.domain.repository.MenuRepository;
//import coffeeshout.room.domain.repository.RoomRepository;
//import coffeeshout.room.ui.request.MenuChangeMessage;
//import coffeeshout.room.ui.request.MiniGameSelectMessage;
//import coffeeshout.room.ui.response.PlayerResponse;
//import coffeeshout.room.ui.response.ProbabilityResponse;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.lang.reflect.Type;
//import java.util.List;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.converter.MappingJackson2MessageConverter;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaders;
//import org.springframework.messaging.simp.stomp.StompSession;
//import org.springframework.messaging.simp.stomp.StompSessionHandler;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.web.socket.client.WebSocketClient;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.messaging.WebSocketStompClient;
//import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
//import org.springframework.web.socket.sockjs.client.SockJsClient;
//import org.springframework.web.socket.sockjs.client.Transport;
//import org.springframework.web.socket.sockjs.client.WebSocketTransport;
//
//@ActiveProfiles("test")
//class RoomWebSocketControllerTest extends WebSocketIntegrationTestSupport {
//
//    @Autowired
//    private RoomRepository roomRepository;
//
//    @Autowired
//    private MenuRepository menuRepository;
//
//    private Room testRoom;
//    private Menu testMenu;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @BeforeEach
//    void setUp() {
//        setupTestData();
//    }
//
//    @Test
//    void 방_입장_시나리오_getPlayers_요청() throws Exception {
//        // given
//        String joinCode = testRoom.getJoinCode().value();
//        BlockingQueue<WebSocketResponse<List<PlayerResponse>>> responseQueue = new LinkedBlockingQueue<>();
//
//        // when - 방 토픽 구독
//        session.subscribe("/topic/room/" + joinCode, getHandler(responseQueue,
//                new TypeReference<WebSocketResponse<List<PlayerResponse>>>() {
//                }));
//
//        // getPlayers 요청 메시지 전송
//        session.send("/app/room/" + joinCode + "/players", null);
//
//        // then - 플레이어 목록 응답 확인
//        WebSocketResponse<List<PlayerResponse>> players = responseQueue.poll(5, TimeUnit.SECONDS);
//
//        assertThat(players).isNotNull();
//        assertThat(players.data()).hasSize(4); // 호스트 + 게스트 3명
//
//        // 호스트 확인
//        PlayerResponse host = players.data().stream().filter(p -> p.playerName().equals("호스트꾹이")).findFirst()
//                .orElseThrow(() -> new AssertionError("호스트를 찾을 수 없음"));
//
//        assertThat(host.menuResponse().name()).isEqualTo("아메리카노");
//        assertThat(host.menuResponse().image()).isEqualTo("americano.jpg");
//
//        // 게스트들 확인
//        List<String> playerNames = players.data().stream().map(PlayerResponse::playerName).toList();
//
//        assertThat(playerNames).containsExactlyInAnyOrder("호스트꾹이", "플레이어한스", "플레이어루키", "플레이어엠제이");
//
//        System.out.println("✅ 플레이어 목록 응답 성공:");
//        players.data().forEach(p -> System.out.println("  - " + p.playerName() + ": " + p.menuResponse().name()));
//    }
//
//    @Test
//    void 여러_클라이언트_플레이어_목록_브로드캐스트() throws Exception {
//        // given - 추가 클라이언트 생성
//        StompSession session2 = getStompSession();
//
//        try {
//            String joinCode = testRoom.getJoinCode().value();
//            BlockingQueue<WebSocketResponse<List<PlayerResponse>>> queue1 = new LinkedBlockingQueue<>();
//            BlockingQueue<WebSocketResponse<List<PlayerResponse>>> queue2 = new LinkedBlockingQueue<>();
//
//            // when - 두 클라이언트 모두 같은 방 구독
//            session.subscribe(
//                    "/topic/room/" + joinCode,
//                    getHandler(queue1, new TypeReference<WebSocketResponse<List<PlayerResponse>>>() {
//                    }));
//            session2.subscribe(
//                    "/topic/room/" + joinCode,
//                    getHandler(queue2, new TypeReference<WebSocketResponse<List<PlayerResponse>>>() {
//                    }));
//
//            // 한 클라이언트에서 플레이어 목록 요청
//            session.send("/app/room/" + joinCode + "/players", null);
//
//            // then - 두 클라이언트 모두 같은 응답 받음
//            WebSocketResponse<List<PlayerResponse>> response1 = queue1.poll(5, TimeUnit.SECONDS);
//            WebSocketResponse<List<PlayerResponse>> response2 = queue2.poll(5, TimeUnit.SECONDS);
//
//            assertThat(response1).isNotNull();
//            assertThat(response2).isNotNull();
//            assertThat(response1.data()).hasSize(4);
//            assertThat(response2.data()).hasSize(4);
//
//            // 두 응답이 동일한지 확인
//            assertThat(response1.data().get(0).playerName()).isEqualTo(response2.data().get(0).playerName());
//
//            System.out.println("✅ 두 클라이언트 모두 동일한 플레이어 목록 수신 성공");
//
//        } finally {
//            session2.disconnect();
//        }
//    }
//
//    @Test
//    void 존재하지_않는_방_ID_요청_테스트() throws Exception {
//        // given
//        String nonExistentJoinCode = "3434X";
//        BlockingQueue<WebSocketResponse<List<PlayerResponse>>> responseQueue = new LinkedBlockingQueue<>();
//
//        // when
//        session.subscribe(
//                "/topic/room/" + nonExistentJoinCode,
//                getHandler(responseQueue, new TypeReference<WebSocketResponse<List<PlayerResponse>>>() {
//                }));
//
//        try {
//            session.send("/app/room/" + nonExistentJoinCode + "/players", null);
//
//            // then - 에러가 발생하거나 응답이 없어야 함
//            WebSocketResponse<List<PlayerResponse>> response = responseQueue.poll(3, TimeUnit.SECONDS);
//
//            // 실제 구현에 따라 null이거나 예외가 발생할 수 있음
//            System.out.println("존재하지 않는 방 요청 응답: " + response);
//
//        } catch (Exception e) {
//            // 예외 발생이 정상적인 경우
//            System.out.println("✅ 예상된 예외 발생: " + e.getMessage());
//        }
//    }
//
//    @Test
//    void 플레이어들의_메뉴를_가져온다() throws Exception {
//        // given
//        String 한스 = "플레이어한스";
//        Long changedMenuId = 2L;
//        String joinCode = testRoom.getJoinCode().value();
//        BlockingQueue<WebSocketResponse<List<PlayerResponse>>> responseQueue = new LinkedBlockingQueue<>();
//        TypeReferenceFrameHandler<WebSocketResponse<List<PlayerResponse>>> handler = getHandler(responseQueue,
//                new TypeReference<WebSocketResponse<List<PlayerResponse>>>() {
//                });
//
//        // when
//        session.subscribe("/topic/room/" + joinCode, handler);
//        MenuChangeMessage message = new MenuChangeMessage(한스, changedMenuId);
//        session.send("/app/room/" + joinCode + "/menus", message);
//
//        // then
//        WebSocketResponse<List<PlayerResponse>> responses = responseQueue.poll(5, TimeUnit.SECONDS);
//        List<PlayerResponse> data = responses.data();
//
//        assertThat(data).hasSize(4);
//
//        Long resultMenuId = responses.data().stream().filter(p -> p.playerName().equals(한스))
//                .findFirst()
//                .get()
//                .menuResponse()
//                .id();
//        assertThat(resultMenuId).isEqualTo(changedMenuId);
//    }
//
//    @Test
//    void 플레이어들의_확률을_반환한다() throws InterruptedException {
//        // given
//        String joinCode = testRoom.getJoinCode().value();
//
//        BlockingQueue<WebSocketResponse<List<ProbabilityResponse>>> responseQueue = new LinkedBlockingQueue<>();
//        TypeReferenceFrameHandler<WebSocketResponse<List<ProbabilityResponse>>> handler = getHandler(responseQueue,
//                new TypeReference<WebSocketResponse<List<ProbabilityResponse>>>() {
//                });
//
//        // when
//        session.subscribe("/topic/room/" + joinCode + "/roulette", handler);
//        session.send("/app/room/" + joinCode + "/probabilities", null);
//
//        WebSocketResponse<List<ProbabilityResponse>> responses = responseQueue.poll(5, TimeUnit.SECONDS);
//
//        // then
//        assertThat(responses.data())
//                .hasSize(4)
//                .allSatisfy(response -> assertThat(response.probability()).isEqualTo(25.0));
//    }
//
//    @Test
//    void 미니게임을_선택한다() throws InterruptedException {
//        // given
//        String joinCode = testRoom.getJoinCode().value();
//
//        BlockingQueue<WebSocketResponse<List<MiniGameType>>> responseQueue = new LinkedBlockingQueue<>();
//        TypeReferenceFrameHandler<WebSocketResponse<List<MiniGameType>>> handler = getHandler(responseQueue,
//                new TypeReference<WebSocketResponse<List<MiniGameType>>>() {
//                });
//
//        // when
//        session.subscribe("/topic/room/" + joinCode + "/minigame", handler);
//        MiniGameSelectMessage message = new MiniGameSelectMessage("호스트꾹이", MiniGameType.CARD_GAME);
//        session.send("/app/room/" + joinCode + "/minigames/select", message);
//
//        WebSocketResponse<List<MiniGameType>> responses = responseQueue.poll(5, TimeUnit.SECONDS);
//
//        // then
//        assertThat(responses.data()).hasSize(1);
//        assertThat(responses.data().get(0)).isEqualTo(MiniGameType.CARD_GAME);
//    }
//
//    @Test
//    void 미니게임을_취소한다() throws InterruptedException {
//        // given
//        String joinCode = testRoom.getJoinCode().value();
//
//        BlockingQueue<WebSocketResponse<List<MiniGameType>>> responseQueue = new LinkedBlockingQueue<>();
//        TypeReferenceFrameHandler<WebSocketResponse<List<MiniGameType>>> handler = getHandler(responseQueue,
//                new TypeReference<WebSocketResponse<List<MiniGameType>>>() {
//                });
//
//        session.subscribe("/topic/room/" + joinCode + "/minigame", handler);
//        MiniGameSelectMessage message = new MiniGameSelectMessage("호스트꾹이", MiniGameType.CARD_GAME);
//        session.send("/app/room/" + joinCode + "/minigames/select", message);
//        WebSocketResponse<List<MiniGameType>> responses = responseQueue.poll(5, TimeUnit.SECONDS);
//        assertThat(responses.data()).hasSize(1);
//        assertThat(responses.data().get(0)).isEqualTo(MiniGameType.CARD_GAME);
//
//        // when
//        MiniGameSelectMessage message2 = new MiniGameSelectMessage("호스트꾹이", MiniGameType.CARD_GAME);
//        session.send("/app/room/" + joinCode + "/minigames/unselect", message2);
//
//        WebSocketResponse<List<MiniGameType>> responses2 = responseQueue.poll(5, TimeUnit.SECONDS);
//
//        // then
//        assertThat(responses2.data()).hasSize(0);
//    }
//
//    private <T> TypeReferenceFrameHandler<T> getHandler(BlockingQueue<T> responseQueue,
//                                                        TypeReference<T> typeReference) {
//        TypeReferenceFrameHandler<T> handler = new TypeReferenceFrameHandler<>(responseQueue, typeReference,
//                objectMapper);
//        return handler;
//    }
//
//    private void setupTestData() {
//        // 메뉴 생성
//        testMenu = menuRepository.findAll().get(0);
//
//        // 방 생성 - 호스트와 함께
//        JoinCode joinCode = new JoinCode("TEST2"); // 5자리로 수정
//        PlayerName hostName = new PlayerName("호스트꾹이");
//        testRoom = Room.createNewRoom(joinCode, hostName, testMenu);
//
//        // 게스트 플레이어들 추가
//        testRoom.joinGuest(new PlayerName("플레이어한스"), testMenu);
//        testRoom.joinGuest(new PlayerName("플레이어루키"), testMenu);
//        testRoom.joinGuest(new PlayerName("플레이어엠제이"), testMenu);
//
//        // 저장 후 실제 ID가 할당된 객체로 다시 받기
//        testRoom = roomRepository.save(testRoom);
//
//        System.out.println("✅ 테스트 방 생성 완료 - JoinCode: " + testRoom.getJoinCode());
//    }
//
//    private static class TestStompSessionHandler implements StompSessionHandler {
//
//        @Override
//        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
//            System.out.println("🔗 SockJS WebSocket 연결 성공");
//        }
//
//        @Override
//        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
//                                    Throwable exception) {
//            System.err.println("❌ STOMP 에러: " + exception.getMessage());
//            exception.printStackTrace();
//        }
//
//        @Override
//        public void handleTransportError(StompSession session, Throwable exception) {
//            System.err.println("❌ 전송 에러: " + exception.getMessage());
//            exception.printStackTrace();
//        }
//
//        @Override
//        public Type getPayloadType(StompHeaders headers) {
//            return String.class;
//        }
//
//        @Override
//        public void handleFrame(StompHeaders headers, Object payload) {
//            System.out.println("📨 프레임 수신: " + payload);
//        }
//
//    }
//
//    private StompSession getStompSession() throws InterruptedException, ExecutionException, TimeoutException {
//        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()),
//                new RestTemplateXhrTransport());
//        WebSocketClient sockJsClient2 = new SockJsClient(transports);
//        WebSocketStompClient stompClient2 = new WebSocketStompClient(sockJsClient2);
//        stompClient2.setMessageConverter(new MappingJackson2MessageConverter());
//
//        StompSession session2 = stompClient2.connectAsync("http://localhost:" + port + "/ws",
//                new TestStompSessionHandler()).get(10, TimeUnit.SECONDS);
//        return session2;
//    }
//}

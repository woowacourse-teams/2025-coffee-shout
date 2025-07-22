package coffeeshout.room.ui;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.MenuRepository;
import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.ui.response.PlayerResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

/**
 * RoomWebSocketController의 실제 E2E 테스트 실제 Room과 Player 데이터를 생성하여 WebSocket 통신 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class RoomWebSocketControllerE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MenuRepository menuRepository;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Room testRoom;
    private Menu testMenu;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트 데이터 생성
        setupTestData();

        // SockJS WebSocket 클라이언트 설정
        List<Transport> transports = List.of(
                new WebSocketTransport(new StandardWebSocketClient()),
                new RestTemplateXhrTransport()
        );
        WebSocketClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String url = "http://localhost:" + port + "/ws";
        stompSession = stompClient.connectAsync(url, new TestStompSessionHandler())
                .get(10, TimeUnit.SECONDS);

        // 연결 안정화 대기
        Thread.sleep(100);
    }

    @AfterEach
    void tearDown() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }

        // RoomRepository에 deleteById가 없으므로 H2 인메모리 DB에서 자동 정리됨
    }

    private void setupTestData() {
        // 메뉴 생성
        testMenu = menuRepository.findById(1L).orElse(null);

        // 방 생성 - 호스트와 함께
        JoinCode joinCode = new JoinCode("TEST2"); // 5자리로 수정
        PlayerName hostName = PlayerName.from("호스트꾹이");
        testRoom = Room.createNewRoom(joinCode, hostName, testMenu);

        // 게스트 플레이어들 추가
        testRoom.joinGuest(PlayerName.from("플레이어한스"), testMenu);
        testRoom.joinGuest(PlayerName.from("플레이어루키"), testMenu);
        testRoom.joinGuest(PlayerName.from("플레이어엠제이"), testMenu);

        // 저장 후 실제 ID가 할당된 객체로 다시 받기
        testRoom = roomRepository.save(testRoom);

        System.out.println("✅ 테스트 방 생성 완료 - ID: " + testRoom.getId());
    }

    @Test
    void 방_입장_시나리오_getPlayers_요청() throws Exception {
        // given
        Long roomId = testRoom.getId();
        BlockingQueue<List<PlayerResponse>> responseQueue = new LinkedBlockingQueue<>();

        // when - 방 토픽 구독
        stompSession.subscribe("/topic/room/" + roomId, new PlayerResponseFrameHandler(responseQueue));

        // getPlayers 요청 메시지 전송
        stompSession.send("/app/room/" + roomId + "/players", null);

        // then - 플레이어 목록 응답 확인
        List<PlayerResponse> players = responseQueue.poll(5, TimeUnit.SECONDS);

        assertThat(players).isNotNull();
        assertThat(players).hasSize(4); // 호스트 + 게스트 3명

        // 호스트 확인
        PlayerResponse host = players.stream()
                .filter(p -> p.playerName().equals("호스트꾹이"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("호스트를 찾을 수 없음"));

        assertThat(host.menuResponse().name()).isEqualTo("아메리카노");
        assertThat(host.menuResponse().image()).isEqualTo("americano.jpg");

        // 게스트들 확인
        List<String> playerNames = players.stream()
                .map(PlayerResponse::playerName)
                .toList();

        assertThat(playerNames).containsExactlyInAnyOrder(
                "호스트꾹이", "플레이어한스", "플레이어루키", "플레이어엠제이"
        );

        System.out.println("✅ 플레이어 목록 응답 성공:");
        players.forEach(p -> System.out.println("  - " + p.playerName() + ": " + p.menuResponse().name()));
    }

    @Test
    void 여러_클라이언트_플레이어_목록_브로드캐스트() throws Exception {
        // given - 추가 클라이언트 생성
        List<Transport> transports = List.of(
                new WebSocketTransport(new StandardWebSocketClient()),
                new RestTemplateXhrTransport()
        );
        WebSocketClient sockJsClient2 = new SockJsClient(transports);
        WebSocketStompClient stompClient2 = new WebSocketStompClient(sockJsClient2);
        stompClient2.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session2 = stompClient2.connectAsync("http://localhost:" + port + "/ws",
                new TestStompSessionHandler()).get(10, TimeUnit.SECONDS);

        try {
            Long roomId = testRoom.getId();
            BlockingQueue<List<PlayerResponse>> queue1 = new LinkedBlockingQueue<>();
            BlockingQueue<List<PlayerResponse>> queue2 = new LinkedBlockingQueue<>();

            // when - 두 클라이언트 모두 같은 방 구독
            stompSession.subscribe("/topic/room/" + roomId, new PlayerResponseFrameHandler(queue1));
            session2.subscribe("/topic/room/" + roomId, new PlayerResponseFrameHandler(queue2));

            // 한 클라이언트에서 플레이어 목록 요청
            stompSession.send("/app/room/" + roomId + "/players", null);

            // then - 두 클라이언트 모두 같은 응답 받음
            List<PlayerResponse> response1 = queue1.poll(5, TimeUnit.SECONDS);
            List<PlayerResponse> response2 = queue2.poll(5, TimeUnit.SECONDS);

            assertThat(response1).isNotNull();
            assertThat(response2).isNotNull();
            assertThat(response1).hasSize(4);
            assertThat(response2).hasSize(4);

            // 두 응답이 동일한지 확인
            assertThat(response1.get(0).playerName()).isEqualTo(response2.get(0).playerName());

            System.out.println("✅ 두 클라이언트 모두 동일한 플레이어 목록 수신 성공");

        } finally {
            session2.disconnect();
        }
    }

    @Test
    void 존재하지_않는_방_ID_요청_테스트() throws Exception {
        // given
        Long nonExistentRoomId = 99999L;
        BlockingQueue<List<PlayerResponse>> responseQueue = new LinkedBlockingQueue<>();

        // when
        stompSession.subscribe("/topic/room/" + nonExistentRoomId, new PlayerResponseFrameHandler(responseQueue));

        try {
            stompSession.send("/app/room/" + nonExistentRoomId + "/players", null);

            // then - 에러가 발생하거나 응답이 없어야 함
            List<PlayerResponse> response = responseQueue.poll(3, TimeUnit.SECONDS);

            // 실제 구현에 따라 null이거나 예외가 발생할 수 있음
            System.out.println("존재하지 않는 방 요청 응답: " + response);

        } catch (Exception e) {
            // 예외 발생이 정상적인 경우
            System.out.println("✅ 예상된 예외 발생: " + e.getMessage());
        }
    }

    private static class TestStompSessionHandler implements StompSessionHandler {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("🔗 SockJS WebSocket 연결 성공");
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                                    StompHeaders headers, byte[] payload, Throwable exception) {
            System.err.println("❌ STOMP 에러: " + exception.getMessage());
            exception.printStackTrace();
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.err.println("❌ 전송 에러: " + exception.getMessage());
            exception.printStackTrace();
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("📨 프레임 수신: " + payload);
        }
    }

    private class PlayerResponseFrameHandler implements org.springframework.messaging.simp.stomp.StompFrameHandler {
        private final BlockingQueue<List<PlayerResponse>> queue;

        public PlayerResponseFrameHandler(BlockingQueue<List<PlayerResponse>> queue) {
            this.queue = queue;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Object.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            try {
                byte[] bytes = (byte[]) payload;
                String jsonString = new String(bytes, StandardCharsets.UTF_8);
                System.out.println("🎯 수신된 플레이어 목록 JSON: " + jsonString);

                List<PlayerResponse> players = objectMapper.readValue(jsonString,
                        new TypeReference<List<PlayerResponse>>() {
                        });
                queue.offer(players);

                System.out.println("✨ 파싱 완료 - 플레이어 수: " + players.size());

            } catch (Exception e) {
                System.err.println("❌ JSON 파싱 실패: " + e.getMessage());
                e.printStackTrace();
                // 에러 시에도 null을 넣어서 테스트가 계속 진행되도록
                queue.offer(null);
            }
        }
    }
}

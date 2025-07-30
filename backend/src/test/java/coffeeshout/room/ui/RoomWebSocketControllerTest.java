package coffeeshout.room.ui;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.fixture.TestStompSession;
import coffeeshout.fixture.TestStompSession.MessageCollector;
import coffeeshout.fixture.WebSocketIntegrationTestSupport;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.MenuType;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.MenuRepository;
import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.ui.request.MenuChangeMessage;
import coffeeshout.room.ui.request.MiniGameSelectMessage;
import coffeeshout.room.ui.response.PlayerResponse;
import coffeeshout.room.ui.response.ProbabilityResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class RoomWebSocketControllerTest extends WebSocketIntegrationTestSupport {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MenuRepository menuRepository;

    private Room testRoom;
    private Menu testMenu;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    @Test
    void 방_입장_시나리오_getPlayers_요청() throws Exception {
        // given
        TestStompSession session = createSession();
        String joinCode = testRoom.getJoinCode().value();

        // when - 방 토픽 구독
        MessageCollector<WebSocketResponse<List<PlayerResponse>>> responses = session.subscribe(
                "/topic/room/" + joinCode,
                new TypeReference<WebSocketResponse<List<PlayerResponse>>>() {
                }
        );

        // getPlayers 요청 메시지 전송
        session.send("/app/room/" + joinCode + "/players", null);

        // then - 플레이어 목록 응답 확인
        List<PlayerResponse> players = responses.get().data();
        assertThat(players).isNotNull();
        assertThat(players).hasSize(4); // 호스트 + 게스트 3명

        // 호스트 확인
        PlayerResponse host = players.stream().filter(p -> p.playerName().equals("호스트꾹이")).findFirst()
                .orElseThrow(() -> new AssertionError("호스트를 찾을 수 없음"));

        assertThat(host.menuResponse().name()).isEqualTo("아메리카노");
        assertThat(host.menuResponse().menuType()).isEqualTo(MenuType.COFFEE);

        // 게스트들 확인
        List<String> playerNames = players.stream().map(PlayerResponse::playerName).toList();

        assertThat(playerNames).containsExactlyInAnyOrder("호스트꾹이", "플레이어한스", "플레이어루키", "플레이어엠제이");

        System.out.println("✅ 플레이어 목록 응답 성공:");
        players.forEach(p -> System.out.println("  - " + p.playerName() + ": " + p.menuResponse().name()));
    }

    @Test
    void 여러_클라이언트_플레이어_목록_브로드캐스트() throws Exception {
        // given - 추가 클라이언트 생성
        TestStompSession session = createSession();
        TestStompSession session2 = createSession();

        String joinCode = testRoom.getJoinCode().value();
        // when - 두 클라이언트 모두 같은 방 구독
        MessageCollector<WebSocketResponse<List<PlayerResponse>>> subscribe1 = session.subscribe(
                "/topic/room/" + joinCode,
                new TypeReference<>() {
                }
        );
        MessageCollector<WebSocketResponse<List<PlayerResponse>>> subscribe2 = session2.subscribe(
                "/topic/room/" + joinCode,
                new TypeReference<>() {
                }
        );

        // 한 클라이언트에서 플레이어 목록 요청
        session.send("/app/room/" + joinCode + "/players", null);

        // then - 두 클라이언트 모두 같은 응답 받음
        List<PlayerResponse> response1 = subscribe1.get().data();
        List<PlayerResponse> response2 = subscribe2.get().data();

        assertThat(response1).isNotNull();
        assertThat(response2).isNotNull();
        assertThat(response1).hasSize(4);
        assertThat(response2).hasSize(4);

        // 두 응답이 동일한지 확인
        assertThat(response1.get(0).playerName()).isEqualTo(response2.get(0).playerName());

        System.out.println("✅ 두 클라이언트 모두 동일한 플레이어 목록 수신 성공");
    }

    @Test
    void 존재하지_않는_방_ID_요청_테스트() throws Exception {
        // given
        TestStompSession session = createSession();
        String nonExistentJoinCode = "3434X";

        // when
        MessageCollector<WebSocketResponse<List<PlayerResponse>>> subscribe = session.subscribe(
                "/topic/room/" + nonExistentJoinCode,
                new TypeReference<WebSocketResponse<List<PlayerResponse>>>() {
                }
        );

        // then
        session.send("/app/room/" + nonExistentJoinCode + "/players", null);

        WebSocketResponse<List<PlayerResponse>> response = subscribe.get();

        // 응답이 실패 상태이거나 데이터가 null이어야 함
        assertThat(response).isNotNull();
        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
    }

    @Test
    void 플레이어들의_메뉴를_가져온다() throws Exception {
        // given
        TestStompSession session = createSession();
        String 한스 = "플레이어한스";
        Long changedMenuId = 2L;
        String joinCode = testRoom.getJoinCode().value();

        // when
        MessageCollector<WebSocketResponse<List<PlayerResponse>>> subscribe = session.subscribe(
                "/topic/room/" + joinCode, new TypeReference<>() {
                }
        );

        MenuChangeMessage message = new MenuChangeMessage(한스, changedMenuId);
        session.send("/app/room/" + joinCode + "/menus", message);

        // then
        List<PlayerResponse> responses = subscribe.get().data();

        assertThat(responses).hasSize(4);

        Long resultMenuId = responses.stream().filter(p -> p.playerName().equals(한스))
                .findFirst()
                .get()
                .menuResponse()
                .id();
        assertThat(resultMenuId).isEqualTo(changedMenuId);
    }

    @Test
    void 플레이어들의_확률을_반환한다() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        TestStompSession session = createSession();
        String joinCode = testRoom.getJoinCode().value();

        // when
        MessageCollector<WebSocketResponse<List<ProbabilityResponse>>> subscribe = session.subscribe(
                "/topic/room/" + joinCode + "/roulette",
                new TypeReference<WebSocketResponse<List<ProbabilityResponse>>>() {
                }
        );

        session.send("/app/room/" + joinCode + "/probabilities", null);

        WebSocketResponse<List<ProbabilityResponse>> responses = subscribe.get();

        // then
        assertThat(responses.data())
                .hasSize(4)
                .allSatisfy(response -> assertThat(response.probability()).isEqualTo(25.0));
    }

    @Test
    void 미니게임을_선택한다() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        TestStompSession session = createSession();
        String joinCode = testRoom.getJoinCode().value();

        // when
        MessageCollector<WebSocketResponse<List<MiniGameType>>> subscribe = session.subscribe(
                "/topic/room/" + joinCode + "/minigame", new TypeReference<>() {
                }
        );

        MiniGameSelectMessage message = new MiniGameSelectMessage("호스트꾹이", List.of(MiniGameType.CARD_GAME));
        session.send("/app/room/" + joinCode + "/minigames/select", message);

        List<MiniGameType> responses = subscribe.get().data();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).isEqualTo(MiniGameType.CARD_GAME);
    }

    @Test
    void 미니게임을_취소한다() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        TestStompSession session = createSession();
        String joinCode = testRoom.getJoinCode().value();

        MessageCollector<WebSocketResponse<List<MiniGameType>>> subscribe = session.subscribe(
                "/topic/room/" + joinCode + "/minigame", new TypeReference<>() {
                }
        );

        MiniGameSelectMessage message = new MiniGameSelectMessage("호스트꾹이", List.of(MiniGameType.CARD_GAME));
        session.send("/app/room/" + joinCode + "/minigames/select", message);

        List<MiniGameType> responses = subscribe.get().data();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).isEqualTo(MiniGameType.CARD_GAME);

        // when
        MiniGameSelectMessage message2 = new MiniGameSelectMessage("호스트꾹이", List.of(MiniGameType.CARD_GAME));
        session.send("/app/room/" + joinCode + "/minigames/unselect", message2);

        List<MiniGameType> responses2 = subscribe.get().data();

        // then
        assertThat(responses2).hasSize(0);
    }

    @Test
    void 룰렛을_돌려서_당첨자를_선택한다() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        TestStompSession session = createSession();
        String joinCode = testRoom.getJoinCode().value();
        String hostName = "호스트꾹이";

        // 미니게임을 먼저 선택
        MessageCollector<WebSocketResponse<List<MiniGameType>>> miniGameSubscribe = session.subscribe(
                "/topic/room/" + joinCode + "/minigame", new TypeReference<>() {
                }
        );

        MiniGameSelectMessage selectMessage = new MiniGameSelectMessage(hostName, List.of(MiniGameType.CARD_GAME));
        session.send("/app/room/" + joinCode + "/minigames/select", selectMessage);

        // 미니게임 선택 응답 확인
        List<MiniGameType> selectedGames = miniGameSubscribe.get().data();
        assertThat(selectedGames).hasSize(1);

        // 미니게임을 시작해서 방 상태를 PLAYING으로 변경
        testRoom.startNextGame(hostName);

        // when - 룰렛 결과 구독
        MessageCollector<WebSocketResponse<PlayerResponse>> rouletteSubscribe = session.subscribe(
                "/topic/room/" + joinCode + "/roulette",
                new TypeReference<WebSocketResponse<PlayerResponse>>() {
                }
        );

        session.send("/app/room/" + joinCode + "/roulette/spin", hostName);

        WebSocketResponse<PlayerResponse> response = rouletteSubscribe.get();

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNotNull();

        PlayerResponse winner = response.data();
        assertThat(winner.playerName()).isNotBlank();
        assertThat(winner.menuResponse()).isNotNull();

        // 선택된 패배자는 방에 있는 플레이어 중 하나여야 함
        List<String> playerNames = List.of("호스트꾹이", "플레이어한스", "플레이어루키", "플레이어엠제이");
        assertThat(playerNames).contains(winner.playerName());
    }

    private void setupTestData() {
        // 메뉴 생성
        testMenu = menuRepository.findAll().get(0);

        // 방 생성 - 호스트와 함께
        JoinCode joinCode = new JoinCode("TEST2"); // 5자리로 수정
        PlayerName hostName = new PlayerName("호스트꾹이");
        testRoom = Room.createNewRoom(joinCode, hostName, testMenu);

        // 게스트 플레이어들 추가
        testRoom.joinGuest(new PlayerName("플레이어한스"), testMenu);
        testRoom.joinGuest(new PlayerName("플레이어루키"), testMenu);
        testRoom.joinGuest(new PlayerName("플레이어엠제이"), testMenu);

        // 저장 후 실제 ID가 할당된 객체로 다시 받기
        testRoom = roomRepository.save(testRoom);

        System.out.println("✅ 테스트 방 생성 완료 - JoinCode: " + testRoom.getJoinCode());
    }
}

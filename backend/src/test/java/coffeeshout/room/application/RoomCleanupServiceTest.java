package coffeeshout.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import coffeeshout.global.websocket.StompSessionManager;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Menu;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.repository.MenuRepository;
import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.domain.service.JoinCodeGenerator;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * 테스트 시에는 1시간 간격이 아닌 500ms 간격으로 실행되도록 설정되어 있다.
 */

@ActiveProfiles("test")
@SpringBootTest
class RoomCleanupServiceTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("room.cleanup.enabled", () -> "true");
    }

    @Value("${room.cleanup.interval}")
    Duration delayMs;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MenuRepository menuRepository;

    @Autowired
    StompSessionManager stompSessionManager;

    @Autowired
    JoinCodeGenerator joinCodeGenerator;

    JoinCode joinCode;
    Room testRoom;
    Menu testMenu;

    @BeforeEach
    void setUp() {
        testMenu = menuRepository.findAll().getFirst();

        // 각 테스트마다 고유한 joinCode 생성하여 테스트 격리
        joinCode = joinCodeGenerator.generate();
        PlayerName hostName = new PlayerName("테스트호스트");
        testRoom = Room.createNewRoom(joinCode, hostName, testMenu);
        testRoom = roomRepository.save(testRoom);
    }

    @Test
    @DisplayName("WebSocket 연결이 있는 룸은 스케줄러 실행 후에도 정리되지 않는다")
    void WebSocket_연결이_있는_룸은_스케줄러_실행_후에도_정리되지_않는다() {
        // given - 플레이어 세션 등록으로 활성 연결 시뮬레이션
        String playerName = "테스트플레이어";
        String sessionId = "test-session-" + System.currentTimeMillis();

        stompSessionManager.registerPlayerSession(joinCode.value(), playerName, sessionId);

        // when & then - 스케줄러가 실행되는 동안 정
        await().pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(delayMs.multipliedBy(2))
                .during(delayMs)
                .untilAsserted(() ->
                        assertThat(roomRepository.findByJoinCode(testRoom.getJoinCode())).isNotEmpty()
                );

        // cleanup
        stompSessionManager.removeSession(sessionId);
    }

    @Test
    @DisplayName("WebSocket 연결이 없는 룸은 스케줄러에 의해 자동으로 정리된다")
    void WebSocket_연결이_없는_룸은_스케줄러에_의해_자동으로_정리된다() {
        // given - 룸이 존재하지만 활성 연결이 없는 상태
        assertThat(roomRepository.findByJoinCode(testRoom.getJoinCode())).isNotEmpty();

        // when & then - 스케줄러가 실행되어 룸이 삭제될 때까지 대기
        // cleanupIntervalMs는 스케줄러의 실행 간격이므로, 최소 한 번의 실행을 위해 여유시간 추가
        await().pollInterval(50, TimeUnit.MILLISECONDS)
                .atMost(delayMs.plus(Duration.ofMillis(300)))
                .untilAsserted(() ->
                        assertThat(roomRepository.findByJoinCode(testRoom.getJoinCode())).isEmpty()
                );
    }

    @Test
    @DisplayName("스케줄러는 연결 상태에 따라 룸을 선택적으로 정리한다")
    void 스케줄러는_연결_상태에_따라_룸을_선택적으로_정리한다() {
        // given - 여러 룸 생성 (고유한 joinCode 사용)
        String connectedRoomCode = joinCodeGenerator.generate().value();
        Room connectedRoom = Room.createNewRoom(new JoinCode(connectedRoomCode), new PlayerName("연결된호스트"), testMenu);
        connectedRoom = roomRepository.save(connectedRoom);

        String disconnectedRoomCode1 = joinCodeGenerator.generate().value();
        Room disconnectedRoom1 = Room.createNewRoom(new JoinCode(disconnectedRoomCode1), new PlayerName("연결안된호스트1"),
                testMenu);
        disconnectedRoom1 = roomRepository.save(disconnectedRoom1);

        String disconnectedRoomCode2 = joinCodeGenerator.generate().value();
        Room disconnectedRoom2 = Room.createNewRoom(new JoinCode(disconnectedRoomCode2), new PlayerName("연결안된호스트2"),
                testMenu);
        disconnectedRoom2 = roomRepository.save(disconnectedRoom2);

        // 하나의 룸에만 플레이어 세션 등록
        String sessionId = "connected-session-" + System.currentTimeMillis();
        stompSessionManager.registerPlayerSession(connectedRoomCode, "연결된플레이어", sessionId);

        // when & then - 스케줄러 실행을 기다리며 선택적 삭제 확인
        final Room finalConnectedRoom = connectedRoom;
        final Room finalDisconnectedRoom = disconnectedRoom1;
        final Room finalDisconnectedRoom1 = disconnectedRoom2;
        await().pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(delayMs.plus(Duration.ofMillis(500)))
                .untilAsserted(() -> {
                    SoftAssertions.assertSoftly(softly -> {
                        // 연결된 룸은 삭제되지 않아야 함
                        softly.assertThat(roomRepository.findByJoinCode(finalConnectedRoom.getJoinCode()))
                                .isNotEmpty();

                        // 연결되지 않은 룸들은 삭제되어야 함
                        softly.assertThat(roomRepository.findByJoinCode(finalDisconnectedRoom.getJoinCode()))
                                .isEmpty();
                        softly.assertThat(roomRepository.findByJoinCode(finalDisconnectedRoom1.getJoinCode()))
                                .isEmpty();
                        softly.assertThat(roomRepository.findByJoinCode(testRoom.getJoinCode()))
                                .isEmpty(); // setUp에서 생성된 룸도 삭제
                    });
                });

        // cleanup
        stompSessionManager.removeSession(sessionId);
    }

    @Test
    @DisplayName("스케줄러는 지속적으로 실행되어 새로 생성된 빈 룸도 정리한다")
    void 스케줄러는_지속적으로_실행되어_새로_생성된_빈_룸도_정리한다() {
        // given - 초기 룸은 먼저 삭제되도록 대기
        await().atMost(delayMs.plus(Duration.ofMillis(300)))
                .untilAsserted(() ->
                        assertThat(roomRepository.findByJoinCode(testRoom.getJoinCode())).isEmpty()
                );

        // when - 새로운 룸 생성 (연결 없음, 고유한 joinCode 사용)
        JoinCode newJoincode = joinCodeGenerator.generate();
        Room newRoom = Room.createNewRoom(newJoincode, new PlayerName("새로운호스트"), testMenu);
        newRoom = roomRepository.save(newRoom);

        // then - 새로 생성된 룸도 다음 스케줄러 실행에서 삭제되는지 확인
        final Room finalNewRoom = newRoom;
        await().pollInterval(50, TimeUnit.MILLISECONDS)
                .atMost(delayMs.plus(Duration.ofMillis(300)))
                .untilAsserted(() ->
                        assertThat(roomRepository.findByJoinCode(finalNewRoom.getJoinCode())).isEmpty()
                );
    }
}
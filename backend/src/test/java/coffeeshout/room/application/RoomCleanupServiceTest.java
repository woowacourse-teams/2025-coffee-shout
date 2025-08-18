package coffeeshout.room.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
        registry.add("room.cleanup.enabled", () -> true);
    }

    @Value("${room.cleanup.interval}")
    Duration delayMs;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MenuRepository menuRepository;


    @Autowired
    JoinCodeGenerator joinCodeGenerator;

    JoinCode joinCode;
    Room testRoom;
    Menu testMenu;

    @BeforeEach
    void setUp() {
        testMenu = menuRepository.findAll().getFirst();

        // 각 테스트마다 고유한 joinCode 생성하여 테스트 격리
        testRoom = createAndSaveRoom("테스트호스트");
        joinCode = testRoom.getJoinCode();
    }

    @Test
    void 플레이어가_있는_룸은_스케줄러_실행_후에도_정리되지_않는다() {
        // given - 게스트 플레이어 추가
        testRoom.joinGuest(new PlayerName("테스트게스트"), testMenu);
        roomRepository.save(testRoom);

        // when & then - 스케줄러가 실행되는 동안 정리되지 않음을 확인
        await().pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(delayMs.plus(Duration.ofMillis(100)))
                .untilAsserted(() ->
                        assertThat(roomRepository.findByJoinCode(testRoom.getJoinCode())).isNotEmpty()
                );
    }

    @Test
    void 호스트만_남은_빈_룸은_스케줄러에_의해_자동으로_정리된다() {
        // given - 룸이 존재하지만 호스트만 있어서 실질적으로 빈 상태
        // (테스트용 룸은 호스트만 있는 상태로 생성되엀만, 빈 상태로 인식되도록 호스트도 제거)
        testRoom.removePlayer(testRoom.getHost().getName());
        roomRepository.save(testRoom);
        
        assertThat(roomRepository.findByJoinCode(testRoom.getJoinCode())).isNotEmpty();

        // when & then - 스케줄러가 실행되어 룸이 삭제될 때까지 대기
        await().pollInterval(50, TimeUnit.MILLISECONDS)
                .atMost(delayMs.plus(Duration.ofMillis(300)))
                .untilAsserted(() ->
                        assertThat(roomRepository.findByJoinCode(testRoom.getJoinCode())).isEmpty()
                );
    }

    @Test
    void 스케줄러는_플레이어_존재_여부에_따라_룸을_선택적으로_정리한다() {
        // given - 여러 룸 생성
        Room roomWithPlayers = createAndSaveRoom("플레이어있는호스트");
        Room emptyRoom1 = createAndSaveRoom("빈방호스트1");
        Room emptyRoom2 = createAndSaveRoom("빈방호스트2");

        // 하나의 룸에만 게스트 플레이어 추가
        roomWithPlayers.joinGuest(new PlayerName("게스트플레이어"), testMenu);
        roomRepository.save(roomWithPlayers);
        
        // 나머지 룸들은 호스트만 있는 상태에서 플레이어를 모두 제거
        emptyRoom1.removePlayer(emptyRoom1.getHost().getName());
        emptyRoom2.removePlayer(emptyRoom2.getHost().getName());
        testRoom.removePlayer(testRoom.getHost().getName());
        roomRepository.save(emptyRoom1);
        roomRepository.save(emptyRoom2);
        roomRepository.save(testRoom);

        // when & then - 스케줄러 실행을 기다리며 선택적 삭제 확인
        await().pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(delayMs.multipliedBy(2))
                .untilAsserted(() -> {
                    SoftAssertions.assertSoftly(softly -> {
                        // 플레이어가 있는 룸은 삭제되지 않아야 함
                        softly.assertThat(roomRepository.findByJoinCode(roomWithPlayers.getJoinCode()))
                                .isNotEmpty();

                        // 빈 룸들은 삭제되어야 함
                        softly.assertThat(roomRepository.findByJoinCode(emptyRoom1.getJoinCode()))
                                .isEmpty();
                        softly.assertThat(roomRepository.findByJoinCode(emptyRoom2.getJoinCode()))
                                .isEmpty();
                        softly.assertThat(roomRepository.findByJoinCode(testRoom.getJoinCode()))
                                .isEmpty();
                    });
                });
    }

    @Test
    void 스케줄러는_지속적으로_실행되어_새로_생성된_빈_룸도_정리한다() {
        // given - 초기 룸을 빈 상태로 만들고 삭제되도록 대기
        testRoom.removePlayer(testRoom.getHost().getName());
        roomRepository.save(testRoom);
        
        await().atMost(delayMs.plus(Duration.ofMillis(300)))
                .untilAsserted(() ->
                        assertThat(roomRepository.findByJoinCode(testRoom.getJoinCode())).isEmpty()
                );

        // when - 새로운 빈 룸 생성
        final Room newRoom = createAndSaveRoom("새로운호스트");
        newRoom.removePlayer(newRoom.getHost().getName());
        roomRepository.save(newRoom);

        // then - 새로 생성된 빈 룸도 다음 스케줄러 실행에서 삭제되는지 확인
        await().pollInterval(50, TimeUnit.MILLISECONDS)
                .atMost(delayMs.plus(Duration.ofMillis(300)))
                .untilAsserted(() ->
                        assertThat(roomRepository.findByJoinCode(newRoom.getJoinCode())).isEmpty()
                );
    }

    private Room createAndSaveRoom(String hostName) {
        String roomCode = joinCodeGenerator.generate().value();
        Room room = Room.createNewRoom(new JoinCode(roomCode), new PlayerName(hostName), testMenu);
        return roomRepository.save(room);
    }
}
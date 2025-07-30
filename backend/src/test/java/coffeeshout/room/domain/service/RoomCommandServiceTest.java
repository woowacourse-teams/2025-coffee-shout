package coffeeshout.room.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import coffeeshout.fixture.RoomFixture;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.repository.RoomRepository;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.task.scheduling.pool.size=1"
})
class RoomCommandServiceTest {

    @Autowired
    private RoomCommandService roomCleanupService;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void 지연시간_이후에_삭제된다() {
        // given
        Room savedRoom = roomRepository.save(RoomFixture.호스트_꾹이());

        // when
        roomCleanupService.delayCleanUp(savedRoom, Duration.ofMillis(50));

        // 즉시는 존재해야 함
        assertThat(roomRepository.findByJoinCode(savedRoom.getJoinCode()))
                .isPresent();

        // 3초 대기 후 삭제 확인
        await().atMost(101, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(roomRepository.findByJoinCode(savedRoom.getJoinCode()))
                        .isEmpty());
    }
}

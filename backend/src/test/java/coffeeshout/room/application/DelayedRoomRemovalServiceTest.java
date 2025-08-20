package coffeeshout.room.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.service.RoomCommandService;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class DelayedRoomRemovalServiceTest {

    @MockitoBean
    RoomCommandService roomCommandService;

    @MockitoBean
    TaskScheduler taskScheduler;

    @Mock
    @SuppressWarnings("rawtypes")
    ScheduledFuture scheduledFuture;

    private DelayedRoomRemovalService delayedRoomRemovalService;

    @BeforeEach
    void setUp() {
        Duration removalDelay = Duration.ofMillis(500);

        delayedRoomRemovalService = new DelayedRoomRemovalService(
                taskScheduler,
                removalDelay,
                roomCommandService
        );
    }

    @Nested
    class 방_지연_삭제_스케줄링 {

        @Test
        @SuppressWarnings("unchecked")
        void 정상적으로_지연_삭제를_스케줄링한다() {
            JoinCode joinCode = new JoinCode("ABCDE");
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willReturn(scheduledFuture);

            delayedRoomRemovalService.scheduleRemoveRoom(joinCode);

            then(taskScheduler).should().schedule(any(Runnable.class), any(Instant.class));
        }

        @Test
        @SuppressWarnings("unchecked")
        void 서로_다른_방은_독립적으로_스케줄링된다() {
            JoinCode joinCode1 = new JoinCode("ABCDE");
            JoinCode joinCode2 = new JoinCode("FGHKJ");
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willReturn(scheduledFuture);

            delayedRoomRemovalService.scheduleRemoveRoom(joinCode1);
            delayedRoomRemovalService.scheduleRemoveRoom(joinCode2);

            then(taskScheduler).should(times(2)).schedule(any(Runnable.class), any(Instant.class));
        }
    }

    @Nested
    class 실제_삭제_실행_시뮬레이션 {

        @Test
        @SuppressWarnings("unchecked")
        void RoomCommandService가_정상_호출된다() {
            JoinCode joinCode = new JoinCode("ABCDE");
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willAnswer(invocation -> {
                        Runnable task = invocation.getArgument(0);
                        task.run();
                        return scheduledFuture;
                    });

            delayedRoomRemovalService.scheduleRemoveRoom(joinCode);

            then(roomCommandService).should().delete(joinCode);
        }
    }
}

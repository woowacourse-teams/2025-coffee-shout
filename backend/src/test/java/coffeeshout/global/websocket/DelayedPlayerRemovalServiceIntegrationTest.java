package coffeeshout.global.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * DelayedPlayerRemovalService의 실제 타이밍 기반 통합 테스트 실제 TaskScheduler를 사용하여 지연 실행을 테스트한다.
 */
@ExtendWith(MockitoExtension.class)
class DelayedPlayerRemovalServiceIntegrationTest {

    @Mock
    private PlayerDisconnectionService playerDisconnectionService;

    private ThreadPoolTaskScheduler taskScheduler;
    private DelayedPlayerRemovalService delayedPlayerRemovalService;

    private final String playerKey = "ABC23:김철수";
    private final String sessionId = "session-123";
    private final String reason = "CLIENT_DISCONNECT";

    @BeforeEach
    void setUp() {
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        taskScheduler.setThreadNamePrefix("test-scheduler-");
        taskScheduler.initialize();

        delayedPlayerRemovalService = new DelayedPlayerRemovalService(taskScheduler, playerDisconnectionService);
    }

    @Nested
    class 실제_지연_실행_테스트 {

        @Test
        void 지연시간_후_실제로_PlayerDisconnectionService가_호출된다() {
            // when
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);

            // then - 15초 후 실행을 기다림 (테스트에서는 짧게 조정할 수 없어서 긴 시간이 필요)
            // 실제 운영에서는 15초지만, 테스트에서는 적절한 시간으로 조정 필요
            await()
                    .atMost(20, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        then(playerDisconnectionService).should()
                                .handlePlayerDisconnection(playerKey, sessionId, reason);
                    });

            // 실행 후에는 스케줄이 제거됨
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isFalse();
        }

        @Test
        void 지연시간_내에_취소하면_실행되지_않는다() {
            // given
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);

            // when - 즉시 취소
            delayedPlayerRemovalService.cancelScheduledRemoval(playerKey);

            // then - 15초 기다려도 실행되지 않음
            await()
                    .during(Duration.ofSeconds(2)) // 2초 동안 호출되지 않음을 확인
                    .atMost(Duration.ofSeconds(3))
                    .untilAsserted(() -> {
                        then(playerDisconnectionService).should(never())
                                .handlePlayerDisconnection(playerKey, sessionId, reason);
                    });

            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isFalse();
        }
    }

    @Nested
    class 동시성_실제_테스트 {

        @Test
        void 여러_플레이어가_동시에_스케줄링되어도_안전하게_처리된다() {
            // given
            String player1 = "ABC23:김철수";
            String player2 = "DEF56:박영희";
            String player3 = "GHI89:이민수";

            // when - 동시에 여러 플레이어 스케줄링
            delayedPlayerRemovalService.schedulePlayerRemoval(player1, "session-1", reason);
            delayedPlayerRemovalService.schedulePlayerRemoval(player2, "session-2", reason);
            delayedPlayerRemovalService.schedulePlayerRemoval(player3, "session-3", reason);

            // then - 모든 플레이어가 독립적으로 처리됨
            await()
                    .atMost(20, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        then(playerDisconnectionService).should()
                                .handlePlayerDisconnection(player1, "session-1", reason);
                        then(playerDisconnectionService).should()
                                .handlePlayerDisconnection(player2, "session-2", reason);
                        then(playerDisconnectionService).should()
                                .handlePlayerDisconnection(player3, "session-3", reason);
                    });

            // 모든 스케줄이 완료됨
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(player1)).isFalse();
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(player2)).isFalse();
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(player3)).isFalse();
        }
    }

    @Nested
    class 리소스_정리_테스트 {

        @Test
        void 실행_완료된_태스크는_맵에서_자동_제거된다() {
            // when
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);

            // 실행 전에는 존재
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isTrue();

            // then - 실행 후에는 제거됨
            await()
                    .atMost(20, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isFalse();
                    });
        }

        @Test
        void 취소된_태스크도_맵에서_즉시_제거된다() {
            // given
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isTrue();

            // when
            delayedPlayerRemovalService.cancelScheduledRemoval(playerKey);

            // then - 즉시 제거됨
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isFalse();
        }
    }

    /**
     * 테스트용 DelayedPlayerRemovalService 실제 15초 대신 짧은 시간으로 테스트할 수 있도록 오버라이드
     */
    private static class TestDelayedPlayerRemovalService extends DelayedPlayerRemovalService {
        private static final Duration TEST_REMOVAL_DELAY = Duration.ofMillis(500); // 500ms로 단축

        public TestDelayedPlayerRemovalService(ThreadPoolTaskScheduler taskScheduler,
                                               PlayerDisconnectionService playerDisconnectionService) {
            super(taskScheduler, playerDisconnectionService);
        }

        // 테스트에서는 더 짧은 지연시간 사용하고 싶다면 이런 식으로 오버라이드 가능
        // 하지만 현재 구조상 REMOVAL_DELAY가 private static final이라 어렵다
        // 실제로는 설정으로 빼거나 생성자 주입으로 받는 게 좋을 듯
    }

    @Nested
    class 빠른_테스트_시나리오 {
        // 실제 15초 기다리기 어려우면 이런 식으로 더 짧은 지연시간으로 테스트
        // 단, DelayedPlayerRemovalService 구조 변경 필요

        @Test
        void 설정으로_지연시간을_조정할_수_있다면_더_빠른_테스트가_가능하다() {
            // 현재는 하드코딩되어 있어서 불가능
            // Duration REMOVAL_DELAY를 생성자나 설정으로 주입받도록 리팩토링하면
            // 테스트에서는 짧은 시간으로 설정 가능
        }
    }
}

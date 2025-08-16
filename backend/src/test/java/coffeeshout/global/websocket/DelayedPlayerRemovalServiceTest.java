package coffeeshout.global.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

@ExtendWith(MockitoExtension.class)
class DelayedPlayerRemovalServiceTest {

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private PlayerDisconnectionService playerDisconnectionService;

    @Mock
    @SuppressWarnings("rawtypes")
    private ScheduledFuture scheduledFuture;

    private DelayedPlayerRemovalService delayedPlayerRemovalService;

    private final String playerKey = "ABC23:김철수";
    private final String sessionId = "session-123";
    private final String reason = "CLIENT_DISCONNECT";

    @BeforeEach
    void setUp() {
        delayedPlayerRemovalService = new DelayedPlayerRemovalService(taskScheduler, playerDisconnectionService);
    }

    @Nested
    class 플레이어_지연_삭제_스케줄링 {

        @Test
        @SuppressWarnings("unchecked")
        void 정상적으로_지연_삭제를_스케줄링한다() {
            // given
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willReturn(scheduledFuture);

            // when
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);

            // then
            then(taskScheduler).should().schedule(any(Runnable.class), any(Instant.class));
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isTrue();
        }

        @Test
        @SuppressWarnings("unchecked")
        void 동일한_플레이어의_기존_스케줄을_취소하고_새로_등록한다() {
            // given
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willReturn(scheduledFuture);

            // 첫 번째 스케줄 등록
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);

            // when - 같은 플레이어 다시 스케줄링
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, "new-session", "SERVER_ERROR");

            // then
            then(taskScheduler).should(times(2)).schedule(any(Runnable.class), any(Instant.class));
            then(scheduledFuture).should().cancel(false);
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isTrue();
        }

        @Test
        @SuppressWarnings("unchecked")
        void 서로_다른_플레이어는_독립적으로_스케줄링된다() {
            // given
            String anotherPlayerKey = "DEF456:박영희";
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willReturn(scheduledFuture);

            // when
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);
            delayedPlayerRemovalService.schedulePlayerRemoval(anotherPlayerKey, "session-456", reason);

            // then
            then(taskScheduler).should(times(2)).schedule(any(Runnable.class), any(Instant.class));
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isTrue();
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(anotherPlayerKey)).isTrue();
        }
    }

    @Nested
    class 지연_삭제_취소 {

        @Test
        @SuppressWarnings("unchecked")
        void 스케줄된_삭제를_정상적으로_취소한다() {
            // given
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willReturn(scheduledFuture);
            given(scheduledFuture.isDone()).willReturn(false);

            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);

            // when
            delayedPlayerRemovalService.cancelScheduledRemoval(playerKey);

            // then
            then(scheduledFuture).should().cancel(false);
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isFalse();
        }

        @Test
        @SuppressWarnings("unchecked")
        void 이미_완료된_스케줄은_취소하지_않는다() {
            // given
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willReturn(scheduledFuture);
            given(scheduledFuture.isDone()).willReturn(true);

            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);

            // when
            delayedPlayerRemovalService.cancelScheduledRemoval(playerKey);

            // then
            then(scheduledFuture).should(never()).cancel(false);
        }

        @Test
        void 존재하지_않는_플레이어의_취소_요청은_무시한다() {
            // when
            delayedPlayerRemovalService.cancelScheduledRemoval("없는플레이어");

            // then - 예외 발생하지 않고 정상 처리
            then(scheduledFuture).should(never()).cancel(false);
        }
    }

    @Nested
    class 스케줄_상태_확인 {

        @Test
        void 스케줄이_없으면_false를_반환한다() {
            // when & then
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isFalse();
        }

        @Test
        @SuppressWarnings("unchecked")
        void 스케줄이_있고_완료되지_않았으면_true를_반환한다() {
            // given
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willReturn(scheduledFuture);
            given(scheduledFuture.isDone()).willReturn(false);

            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);

            // when & then
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isTrue();
        }

        @Test
        @SuppressWarnings("unchecked")
        void 스케줄이_완료되었으면_false를_반환한다() {
            // given
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willReturn(scheduledFuture);
            given(scheduledFuture.isDone()).willReturn(true);

            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);

            // when & then
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isFalse();
        }
    }

    @Nested
    class 실제_삭제_실행_시뮬레이션 {

        @Test
        @SuppressWarnings("unchecked")
        void PlayerDisconnectionService가_정상_호출된다() {
            // given
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willAnswer(invocation -> {
                        Runnable task = invocation.getArgument(0);
                        // 스케줄링된 태스크를 바로 실행
                        task.run();
                        return scheduledFuture;
                    });

            // when
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);

            // then
            then(playerDisconnectionService).should()
                    .handlePlayerDisconnection(playerKey, sessionId, reason);
        }

        @Test
        @SuppressWarnings("unchecked")
        void PlayerDisconnectionService에서_예외_발생해도_안전하게_처리한다() {
            // given
            willThrow(new RuntimeException("플레이어 삭제 실패"))
                    .given(playerDisconnectionService)
                    .handlePlayerDisconnection(any(), any(), any());

            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willAnswer(invocation -> {
                        Runnable task = invocation.getArgument(0);
                        // 스케줄링된 태스크를 바로 실행
                        task.run();
                        return scheduledFuture;
                    });

            // when & then - 예외가 터져도 프로그램이 죽지 않음
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);

            then(playerDisconnectionService).should()
                    .handlePlayerDisconnection(playerKey, sessionId, reason);
        }
    }

    @Nested
    class 동시성_시나리오 {

        @Test
        @SuppressWarnings("unchecked")
        void 같은_플레이어의_동시_스케줄링_요청을_안전하게_처리한다() {
            // given
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willReturn(scheduledFuture);

            // when - 동시에 여러 번 호출
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId + "2", reason);
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId + "3", reason);

            // then - 마지막 스케줄만 유효
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isTrue();
            then(taskScheduler).should(times(3)).schedule(any(Runnable.class), any(Instant.class));
            then(scheduledFuture).should(times(2)).cancel(false); // 첫 번째, 두 번째 취소됨
        }

        @Test
        @SuppressWarnings("unchecked")
        void 스케줄링_중_취소_요청이_와도_안전하게_처리한다() {
            // given
            given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                    .willReturn(scheduledFuture);
            given(scheduledFuture.isDone()).willReturn(false);

            // when
            delayedPlayerRemovalService.schedulePlayerRemoval(playerKey, sessionId, reason);
            delayedPlayerRemovalService.cancelScheduledRemoval(playerKey);

            // then
            assertThat(delayedPlayerRemovalService.hasScheduledRemoval(playerKey)).isFalse();
            then(scheduledFuture).should().cancel(false);
        }
    }
}

package coffeeshout.profanity.infra.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import coffeeshout.profanity.application.ProfanityFilterService;
import coffeeshout.profanity.config.ProfanityTrieRefreshProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 트라이 재빌드 신호 병합·최소 간격 회귀 테스트(#1759).
 *
 * <p>10만 건 실측에서 트라이 재빌드가 10,028회, 합계 2,272초 돌아 회차 시간(271초)의 8.4배가 겹쳐 돌았다.
 * 리스너 컨테이너 기본 실행기가 신호마다 스레드를 새로 만들어 동시 실행 상한이 없었던 게 원인이다.
 */
class ProfanityTrieRefreshSubscriberTest {

    private static final Message MESSAGE = mock(Message.class);

    private RedisMessageListenerContainer container;
    private ProfanityFilterService filterService;
    private SimpleMeterRegistry meterRegistry;
    private StubClock clock;
    private ProfanityTrieRefreshSubscriber subscriber;

    @BeforeEach
    void setUp() {
        container = mock(RedisMessageListenerContainer.class);
        filterService = mock(ProfanityFilterService.class);
        meterRegistry = new SimpleMeterRegistry();
        clock = new StubClock(Instant.parse("2026-09-06T00:00:00Z"));
    }

    @AfterEach
    void tearDown() {
        subscriber.shutdown();
    }

    private void 구독자를_준비한다(Duration minInterval) {
        subscriber = new ProfanityTrieRefreshSubscriber(
                container, filterService, meterRegistry, clock, new ProfanityTrieRefreshProperties(minInterval));
        subscriber.register();
    }

    @Nested
    @DisplayName("신호 병합")
    class 신호_병합 {

        @Test
        @DisplayName("연속 신호는 재빌드를 신호 횟수만큼 돌리지 않는다")
        void 연속_신호가_병합된다() {
            구독자를_준비한다(Duration.ZERO);

            for (int i = 0; i < 20; i++) {
                subscriber.onMessage(MESSAGE, null);
            }

            await().atMost(Duration.ofSeconds(2))
                    .untilAsserted(() -> verify(filterService, times(1)).rebuildTrie());
            assertThat(meterRegistry
                            .get("profanity.trie.rebuild.coalesced")
                            .counter()
                            .count())
                    .as("병합돼 버려진 신호 수가 기록된다")
                    .isGreaterThan(0);
        }

        @Test
        @DisplayName("동시 실행이 1을 넘지 않는다")
        void 동시_실행이_1을_넘지_않는다() throws InterruptedException {
            구독자를_준비한다(Duration.ZERO);
            final AtomicInteger concurrent = new AtomicInteger(0);
            final AtomicInteger maxConcurrent = new AtomicInteger(0);
            doAnswer(invocation -> {
                        final int current = concurrent.incrementAndGet();
                        maxConcurrent.updateAndGet(max -> Math.max(max, current));
                        // 재빌드가 순간에 끝나지 않음을 흉내낸다. Thread.sleep 대신 만료되는 대기로 막는다.
                        new CountDownLatch(1).await(20, TimeUnit.MILLISECONDS);
                        concurrent.decrementAndGet();
                        return null;
                    })
                    .when(filterService)
                    .rebuildTrie();

            final ExecutorService signalSenders = Executors.newFixedThreadPool(10);
            for (int i = 0; i < 10; i++) {
                signalSenders.execute(() -> subscriber.onMessage(MESSAGE, null));
            }
            signalSenders.shutdown();
            signalSenders.awaitTermination(5, TimeUnit.SECONDS);

            await().atMost(Duration.ofSeconds(2))
                    .untilAsserted(() -> assertThat(concurrent.get()).isZero());
            assertThat(maxConcurrent.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("재빌드 도중 신호")
    class 재빌드_도중_신호 {

        @Test
        @DisplayName("DB를 읽는 동안 들어온 신호는 재빌드를 한 번 더 유발한다")
        void 읽기_도중_신호가_재빌드를_한번_더_유발한다() throws InterruptedException {
            구독자를_준비한다(Duration.ZERO);
            final CountDownLatch enteredRebuild = new CountDownLatch(1);
            final CountDownLatch releaseRebuild = new CountDownLatch(1);
            doAnswer(invocation -> {
                        enteredRebuild.countDown();
                        releaseRebuild.await();
                        return null;
                    })
                    .when(filterService)
                    .rebuildTrie();

            subscriber.onMessage(MESSAGE, null);
            assertThat(enteredRebuild.await(2, TimeUnit.SECONDS))
                    .as("첫 재빌드가 DB를 읽는 중이다")
                    .isTrue();

            // 플래그가 읽기 전에 내려가 있어야 이 신호가 다음 재빌드를 예약한다.
            subscriber.onMessage(MESSAGE, null);
            releaseRebuild.countDown();

            await().atMost(Duration.ofSeconds(2))
                    .untilAsserted(() -> verify(filterService, times(2)).rebuildTrie());
        }
    }

    @Nested
    @DisplayName("최소 간격")
    class 최소_간격 {

        @Test
        @DisplayName("간격 안에 들어온 신호는 간격이 지난 뒤에 반영된다")
        void 간격_안_신호는_지연되어_반영된다() {
            final Duration minInterval = Duration.ofMillis(400);
            구독자를_준비한다(minInterval);

            subscriber.onMessage(MESSAGE, null);
            await().atMost(Duration.ofSeconds(2))
                    .untilAsserted(() -> verify(filterService, times(1)).rebuildTrie());

            subscriber.onMessage(MESSAGE, null);
            // 간격이 지나기 전이라 두 번째 신호는 아직 반영되지 않는다.
            verify(filterService, times(1)).rebuildTrie();

            await().atMost(Duration.ofSeconds(2))
                    .untilAsserted(() -> verify(filterService, times(2)).rebuildTrie());
        }

        @Test
        @DisplayName("간격이 지난 뒤 신호는 대기 없이 반영된다")
        void 간격이_지난_신호는_즉시_반영된다() {
            final Duration minInterval = Duration.ofSeconds(5);
            구독자를_준비한다(minInterval);

            subscriber.onMessage(MESSAGE, null);
            await().atMost(Duration.ofSeconds(2))
                    .untilAsserted(() -> verify(filterService, times(1)).rebuildTrie());

            // 실제 시간은 그대로 두고 주입된 Clock만 간격 이상으로 흘려보낸다.
            clock.advance(minInterval.plusSeconds(1));
            subscriber.onMessage(MESSAGE, null);

            await().atMost(Duration.ofMillis(500))
                    .untilAsserted(() -> verify(filterService, times(2)).rebuildTrie());
        }
    }

    @Nested
    @DisplayName("재빌드 실패")
    class 재빌드_실패 {

        @Test
        @DisplayName("재빌드가 예외를 던져도 다음 신호는 정상 처리된다")
        void 예외_후_다음_신호가_정상_처리된다() {
            구독자를_준비한다(Duration.ZERO);
            doThrow(new RuntimeException("DB 조회 실패"))
                    .doNothing()
                    .when(filterService)
                    .rebuildTrie();

            subscriber.onMessage(MESSAGE, null);
            await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> assertThat(meterRegistry
                            .get("profanity.trie.rebuild.failure")
                            .counter()
                            .count())
                    .isEqualTo(1));

            subscriber.onMessage(MESSAGE, null);
            await().atMost(Duration.ofSeconds(2))
                    .untilAsserted(() -> verify(filterService, times(2)).rebuildTrie());
        }
    }

    /** 재빌드 최소 간격 계산을 흉내내기 위한 수동 진행 시계. */
    private static final class StubClock extends Clock {

        private Instant now;

        private StubClock(Instant start) {
            this.now = start;
        }

        void advance(Duration amount) {
            now = now.plus(amount);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}

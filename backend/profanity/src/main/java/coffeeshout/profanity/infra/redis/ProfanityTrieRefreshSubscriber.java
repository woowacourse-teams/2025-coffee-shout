package coffeeshout.profanity.infra.redis;

import coffeeshout.profanity.application.ProfanityFilterService;
import coffeeshout.profanity.config.ProfanityTrieRefreshProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfanityTrieRefreshSubscriber implements MessageListener {

    private final RedisMessageListenerContainer container;
    private final ProfanityFilterService filterService;
    private final MeterRegistry meterRegistry;
    private final Clock clock;
    private final ProfanityTrieRefreshProperties properties;

    /**
     * 재빌드 전용 단독 스레드(#1759). 리스너 컨테이너 기본 실행기({@code SimpleAsyncTaskExecutor})는
     * 신호마다 새 스레드를 만들어 동시 실행 상한이 없다. 재빌드를 이 스레드 하나로 몰아 동시 실행을 1로 묶고,
     * 최소 간격 지연도 여기 위임한다(Thread.sleep 금지 — 고정 대기는 실행 환경에 따라 동작이 갈린다).
     */
    private final ScheduledExecutorService rebuildExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 재빌드가 이미 예약(대기 또는 실행 중)됐는지 표시한다. 신호에는 payload가 없고 재빌드는 항상 사전
     * 전량을 다시 읽는 통짜 작업이라, 예약된 동안 들어온 신호는 버려도 결과가 같다.
     */
    private final AtomicBoolean rebuildScheduled = new AtomicBoolean(false);

    private volatile Instant lastRebuildStartedAt = Instant.EPOCH;

    private Counter rebuildFailureCounter;
    private Counter rebuildCoalescedCounter;

    @PostConstruct
    public void register() {
        rebuildFailureCounter = Counter.builder("profanity.trie.rebuild.failure")
                .description("비속어 트라이 재빌드 실패 횟수")
                .register(meterRegistry);
        rebuildCoalescedCounter = Counter.builder("profanity.trie.rebuild.coalesced")
                .description("병합으로 버려진 트라이 재빌드 신호 수")
                .register(meterRegistry);
        container.addMessageListener(this, new PatternTopic(ProfanityRedisChannel.TRIE_REFRESH));
        log.info("비속어 트라이 갱신 구독 등록 완료 — channel: {}", ProfanityRedisChannel.TRIE_REFRESH);
    }

    @PreDestroy
    public void shutdown() {
        rebuildExecutor.shutdownNow();
    }

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        log.debug("비속어 트라이 갱신 신호 수신");
        if (rebuildScheduled.compareAndSet(false, true)) {
            rebuildExecutor.schedule(this::rebuild, delayUntilNextRebuildMillis(), TimeUnit.MILLISECONDS);
        } else {
            rebuildCoalescedCounter.increment();
        }
    }

    private long delayUntilNextRebuildMillis() {
        final Duration elapsed = Duration.between(lastRebuildStartedAt, clock.instant());
        final Duration remaining = properties.minInterval().minus(elapsed);
        return remaining.isPositive() ? remaining.toMillis() : 0L;
    }

    private void rebuild() {
        // 플래그는 반드시 DB를 읽기 전에 내린다. 읽은 뒤에 내리면 읽는 도중 커밋된 단어를 놓친다.
        // 읽기 전에 내리면 그 단어의 신호가 플래그를 다시 세워 한 번 더 돌게 되는데, 그게 맞는 동작이다.
        rebuildScheduled.set(false);
        lastRebuildStartedAt = clock.instant();
        try {
            filterService.rebuildTrie();
        } catch (Exception e) {
            log.error("비속어 트라이 재빌드 실패 — channel: {}", ProfanityRedisChannel.TRIE_REFRESH, e);
            rebuildFailureCounter.increment();
        }
    }
}

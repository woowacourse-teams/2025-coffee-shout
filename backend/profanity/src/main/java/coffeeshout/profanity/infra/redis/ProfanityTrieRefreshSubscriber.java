package coffeeshout.profanity.infra.redis;

import coffeeshout.profanity.application.ProfanityFilterService;
import coffeeshout.profanity.config.ProfanityTrieRebuildProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
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

    private static final PatternTopic TRIE_REFRESH_TOPIC = new PatternTopic(ProfanityRedisChannel.TRIE_REFRESH);

    private final RedisMessageListenerContainer container;
    private final ProfanityFilterService filterService;
    private final MeterRegistry meterRegistry;
    private final Clock clock;
    private final ProfanityTrieRebuildProperties properties;

    /**
     * 재빌드 전용 단독 스레드(#1759). 리스너 컨테이너 기본 실행기인 {@code SimpleAsyncTaskExecutor}는
     * 신호마다 새 스레드를 만들어 동시 실행 상한이 없다. 신호 기반 재빌드의 동시 실행을 1로 묶는다.
     * 매시 안전망 재빌드와 시드 로더는 이 실행기를 거치지 않고 별도 스레드에서 rebuildTrie()를 직접
     * 부른다. 최소 간격 지연도 이 실행기에 위임한다. 고정 대기는 실행 환경에 따라 동작이 갈려
     * Thread.sleep은 쓰지 않는다.
     */
    private final ScheduledExecutorService rebuildExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 다음 재빌드가 이미 예약됐는지 표시한다. 신호에는 payload가 없고 재빌드는 항상 사전 전량을
     * 다시 읽는 통짜 작업이라, 예약된 동안 들어온 신호는 버려도 결과가 같다.
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
        container.addMessageListener(this, TRIE_REFRESH_TOPIC);
        log.info("비속어 트라이 갱신 구독 등록 완료 — channel: {}", ProfanityRedisChannel.TRIE_REFRESH);
    }

    /**
     * 리스너를 먼저 해제해 새 신호 유입을 끊은 뒤 실행기를 내린다. 순서를 바꿔 실행기부터 내리면,
     * 스프링이 빈을 의존 역순으로 파괴해 이 구독자가 컨테이너보다 먼저 죽는 사이 들어온 신호가
     * 이미 닫힌 실행기에 예약을 시도해 {@link RejectedExecutionException}이 리스너 스레드로 샌다.
     * 진행 중인 재빌드는 끝까지 마치게 두고, 그래도 안 끝나면 강제 종료한다.
     */
    @PreDestroy
    public void shutdown() {
        container.removeMessageListener(this, TRIE_REFRESH_TOPIC);
        rebuildExecutor.shutdown();
        try {
            if (!rebuildExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                rebuildExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            rebuildExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        log.debug("비속어 트라이 갱신 신호 수신");
        if (rebuildScheduled.compareAndSet(false, true)) {
            try {
                rebuildExecutor.schedule(this::rebuild, delayUntilNextRebuildMillis(), TimeUnit.MILLISECONDS);
            } catch (RejectedExecutionException e) {
                // 실행기가 이미 종료 처리 중이라 예약을 받지 않는다. 플래그를 되돌려도 구독은 이미
                // 해제된 뒤라 다음 신호는 없다.
                rebuildScheduled.set(false);
            }
        } else {
            rebuildCoalescedCounter.increment();
        }
    }

    private long delayUntilNextRebuildMillis() {
        final Duration elapsed = Duration.between(lastRebuildStartedAt, clock.instant());
        final Duration remaining = properties.minInterval().minus(elapsed);
        if (remaining.isNegative() || remaining.isZero()) {
            return 0L;
        }
        // NTP 스텝 조정 등으로 시계가 뒤로 뛰면 elapsed가 음수가 되어 remaining이 minInterval을 넘을 수
        // 있다. ScheduledExecutorService의 대기는 System.nanoTime() 기반이라 시계가 돌아와도 줄어들지
        // 않고, 그 긴 대기 동안 들어오는 신호가 전부 버려진다. 상한을 minInterval로 잡아 막는다.
        if (remaining.compareTo(properties.minInterval()) > 0) {
            return properties.minInterval().toMillis();
        }
        return remaining.toMillis();
    }

    private void rebuild() {
        // 타임스탬프를 플래그보다 먼저 갱신한다. 순서를 바꾸면 플래그가 내려간 뒤 이 두 줄 사이에 들어온
        // 신호가 갱신 전 타임스탬프를 보고, 최소 간격이 이미 지난 것으로 계산해 곧바로 재빌드를 예약한다.
        // 그러면 최소 간격이 무시된다. 타임스탬프를 먼저 쓰면 그 신호도 정확한 간격을 계산한다.
        // 플래그는 여전히 DB를 읽기 전에 내린다. 읽은 뒤에 내리면 읽는 도중 커밋된 단어를 놓친다.
        lastRebuildStartedAt = clock.instant();
        rebuildScheduled.set(false);
        try {
            filterService.rebuildTrie();
        } catch (Exception e) {
            log.error("비속어 트라이 재빌드 실패 — channel: {}", ProfanityRedisChannel.TRIE_REFRESH, e);
            rebuildFailureCounter.increment();
        }
    }
}

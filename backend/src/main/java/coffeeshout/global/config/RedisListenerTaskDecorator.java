package coffeeshout.global.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskDecorator;

/**
 * Redis 리스너 스레드풀의 태스크 실행 시간을 측정하는 TaskDecorator
 * 태스크가 큐에 들어간 시점부터 완료까지의 전체 시간을 메트릭으로 수집
 */
@Slf4j
@RequiredArgsConstructor
public class RedisListenerTaskDecorator implements TaskDecorator {

    private final MeterRegistry meterRegistry;

    @Override
    public Runnable decorate(Runnable runnable) {
        // 태스크가 큐에 들어간 시점 기록
        final long queuedAt = System.nanoTime();

        return () -> {
            // 태스크 실행 시작 시점 (큐에서 꺼내진 시점)
            final long executionStartAt = System.nanoTime();

            // 큐 대기 시간 계산 (나노초 -> 밀리초)
            final long queueWaitTimeMs = (executionStartAt - queuedAt) / 1_000_000;

            try {
                // 태스크 실행 시간 측정
                final Timer.Sample sample = Timer.start(meterRegistry);

                runnable.run();

                // 실행 시간 기록
                sample.stop(Timer.builder("redis.listener.task.execution.time")
                        .description("Redis 리스너 태스크 실행 시간 (큐 대기 시간 제외)")
                        .tag("executor", "redisListenerExecutor")
                        .register(meterRegistry));

                // 태스크 완료 시점
                final long completedAt = System.nanoTime();

                // 전체 시간 (큐 대기 + 실행) 계산 및 기록
                final long totalTimeMs = (completedAt - queuedAt) / 1_000_000;

                Timer.builder("redis.listener.task.total.time")
                        .description("Redis 리스너 태스크 전체 처리 시간 (큐 대기 + 실행)")
                        .tag("executor", "redisListenerExecutor")
                        .register(meterRegistry)
                        .record(totalTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);

                // 큐 대기 시간 기록
                Timer.builder("redis.listener.task.queue.wait.time")
                        .description("Redis 리스너 태스크 큐 대기 시간")
                        .tag("executor", "redisListenerExecutor")
                        .register(meterRegistry)
                        .record(queueWaitTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);

                log.debug("Redis 리스너 태스크 처리 완료 - 큐 대기: {}ms, 실행: {}ms, 전체: {}ms",
                        queueWaitTimeMs, totalTimeMs - queueWaitTimeMs, totalTimeMs);

            } catch (Exception e) {
                log.error("Redis 리스너 태스크 실행 중 오류 발생", e);

                // 에러 카운터
                meterRegistry.counter("redis.listener.task.error",
                        "executor", "redisListenerExecutor",
                        "error.type", e.getClass().getSimpleName())
                        .increment();

                throw e;
            }
        };
    }
}

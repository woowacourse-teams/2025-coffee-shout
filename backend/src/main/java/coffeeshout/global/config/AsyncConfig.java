package coffeeshout.global.config;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

    private final ContextSnapshotFactory snapshotFactory;
    private final ObservationRegistry observationRegistry;

    @Bean(name = "qrCodeTaskExecutor")
    public Executor qrCodeTaskExecutor() {
        try (ExecutorService delegate = Executors.newVirtualThreadPerTaskExecutor()) {
            return (Runnable command) -> {
                final ContextSnapshot snapshot = snapshotFactory.captureAll();
                delegate.execute(snapshot.wrap(() -> {
                    final Observation parent = observationRegistry.getCurrentObservation();
                    if (parent != null) {
                        Observation.createNotStarted("async.qrcode", observationRegistry)
                                .parentObservation(parent)
                                .lowCardinalityKeyValue("thread", Thread.currentThread().getName())
                                .observeChecked(command::run);
                    } else {
                        command.run();
                    }
                }));
            };
        }
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("비동기 작업 실패: method={}, params={}", method.getName(), params, ex);
    }
}

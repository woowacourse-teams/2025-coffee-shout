package coffeeshout.global.config;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebSocketSchedulerConfig {

    @Bean
    @Primary
    public TaskScheduler applicationTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("app-scheduler-");
        scheduler.setTaskDecorator(runnable -> {
            final ContextSnapshot snapshot = ContextSnapshotFactory.builder().build().captureAll();
            return snapshot.wrap(runnable);
        });
        scheduler.initialize();
        return scheduler;
    }
//    @Bean(name = "webSocketHeartBeatScheduler")
//    public TaskScheduler heartBeatMessageBrokerTaskScheduler() {
//        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//        scheduler.setPoolSize(1);
//        scheduler.setThreadNamePrefix("wss-heartbeat-thread-");
//        scheduler.initialize();
//        return scheduler;
//    }
}

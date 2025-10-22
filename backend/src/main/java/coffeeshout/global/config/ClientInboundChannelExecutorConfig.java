package coffeeshout.global.config;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@Slf4j
public class ClientInboundChannelExecutorConfig {

    @Bean(name = "applicationTaskExecutor1")
    @Profile("!test")
    public Executor clientInboundChannelExecutor() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("clientOutboundChannel-");
        scheduler.setDaemon(false);
        scheduler.setErrorHandler(t ->
                log.error("스케줄 실행 중 예외가 발생했습니다.", t)
        );
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);

        scheduler.initialize();
        return scheduler;
    }
}

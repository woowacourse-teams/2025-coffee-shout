package coffeeshout.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@TestConfiguration
@Profile("test")
public class IntegrationTestConfig {

    @Bean(name = "miniGameTaskScheduler")
    public TaskScheduler testIntegrationMiniGameTaskScheduler() {
        return new ShutDownTestScheduler();
    }

    @Bean(name = "delayRemovalScheduler")
    public TaskScheduler testIntegrationDelayRemovalScheduler() {
        return new ShutDownTestScheduler();
    }


}

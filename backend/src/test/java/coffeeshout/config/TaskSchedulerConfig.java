package coffeeshout.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;

@TestConfiguration
public class TaskSchedulerConfig {

    @Bean(name = "miniGameTaskScheduler")
    @Profile("test")
    public TaskScheduler testMiniGameTaskScheduler() {
        return new TestTaskScheduler();
    }
}

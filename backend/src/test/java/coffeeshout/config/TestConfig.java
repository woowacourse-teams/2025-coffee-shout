package coffeeshout.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean(name = "miniGameTaskScheduler")
    public TaskScheduler testMiniGameTaskScheduler() {
        return new TestTaskScheduler();
    }

}

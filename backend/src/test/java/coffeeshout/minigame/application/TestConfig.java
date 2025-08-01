package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.executor.ThreadSleeper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public ThreadSleeper testSleeper() {
        return new ThreadSleeper() {
            @Override
            public void sleep(long milliSecond) {
            }
        };
    }
}

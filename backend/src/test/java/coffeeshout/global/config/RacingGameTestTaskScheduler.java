package coffeeshout.global.config;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class RacingGameTestTaskScheduler extends ThreadPoolTaskScheduler {

    public RacingGameTestTaskScheduler() {
        this.setPoolSize(2);
        this.setThreadNamePrefix("mini-game-task");
        this.setDaemon(false);
        this.setWaitForTasksToCompleteOnShutdown(false);
        this.setAwaitTerminationSeconds(10);
        this.initialize();
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
        return super.schedule(task, Instant.now());
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration duration) {
        return super.scheduleAtFixedRate(task, Duration.ofMillis(1));
    }
}

package coffeeshout.config;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class TestTaskScheduler extends ThreadPoolTaskScheduler {

    public TestTaskScheduler() {
        this.setPoolSize(1);
        this.setThreadNamePrefix("mini-game-task");
        this.setDaemon(false);
        this.setWaitForTasksToCompleteOnShutdown(true);
        this.setAwaitTerminationSeconds(30);
        this.initialize();
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
        return super.schedule(task, Instant.now());
    }
}

package coffeeshout.minigame.domain.temp;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.TaskScheduler;

public class ChainedTask {
    private final Runnable runnable;
    private final Duration delay;
    private ScheduledFuture<?> future;
    private ChainedTask nextChainedTask;

    public ChainedTask(Runnable runnable, Duration delay) {
        this.runnable = runnable;
        this.delay = delay;
    }

    public void start(TaskScheduler scheduler) {
        future = scheduler.schedule(chainedRunnable(scheduler), Instant.now().plus(delay));
    }

    public void setNextTask(ChainedTask nextChainedTask) {
        this.nextChainedTask = nextChainedTask;
    }

    public void join() throws ExecutionException, InterruptedException {
        this.future.get();
        if (nextChainedTask != null) {
            this.nextChainedTask.join();
        }
    }

    public void cancel() {
        if (!isStarted()) {
            return;
        }
        future.cancel(false);
    }

    private Runnable chainedRunnable(TaskScheduler scheduler) {
        if (nextChainedTask == null) {
            return runnable;
        }
        return () -> {
            runnable.run();
            nextChainedTask.start(scheduler);
        };
    }

    private boolean isStarted() {
        return future != null;
    }
}

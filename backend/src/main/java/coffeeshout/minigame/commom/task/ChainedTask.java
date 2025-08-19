package coffeeshout.minigame.commom.task;

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
        future = scheduler.schedule(chainedRunnable(scheduler), Instant.now());
    }

    public void start(TaskScheduler scheduler, Duration delay) {
        future = scheduler.schedule(chainedRunnable(scheduler), Instant.now().plus(delay));
    }

    public void setNextTask(ChainedTask nextChainedTask) {
        this.nextChainedTask = nextChainedTask;
    }

    public void join() throws ExecutionException, InterruptedException {
        this.future.get();
    }

    public void joinAll() throws ExecutionException, InterruptedException {
        this.future.get();
        if (nextChainedTask != null) {
            this.nextChainedTask.joinAll();
        }
    }

    public void cancel() {
        if (!isStarted()) {
            return;
        }
        future.cancel(false);
    }

    public void cancelDelay(TaskScheduler scheduler) {
        if (nextChainedTask == null || !nextChainedTask.isStarted() || nextChainedTask.isDone()) {
            return;
        }
        nextChainedTask.cancel();
        nextChainedTask.start(scheduler);
    }

    public boolean isDone() {
        return isStarted() && future.isDone();
    }

    private Runnable chainedRunnable(TaskScheduler scheduler) {
        if (nextChainedTask == null) {
            return runnable;
        }
        return () -> {
            runnable.run();
            nextChainedTask.start(scheduler, delay);
        };
    }

    private boolean isStarted() {
        return future != null;
    }
}

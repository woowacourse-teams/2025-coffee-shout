package coffeeshout.minigame.domain.temp;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.TaskScheduler;

public class ChainedTaskV2  {
    private final Runnable action;
    private final Duration preDelay;
    private final Duration postDelay;
    private ScheduledFuture<?> future;
    private ChainedTask nextChainedTask;

    private ChainedTaskV2(Builder builder) {
        this.action = builder.action;
        this.preDelay = builder.preDelay;
        this.postDelay = builder.postDelay;
    }

    public void start(TaskScheduler scheduler) {
        Instant startTime = Instant.now().plus(preDelay);

        future = scheduler.schedule(() -> {
            action.run(); // 작업 실행

            if (nextChainedTask != null) {
                // 실행 후 지연 적용
                Instant nextStartTime = postDelay.isZero() ?
                        Instant.now() : Instant.now().plus(postDelay);

                scheduler.schedule(
                        () -> nextChainedTask.start(scheduler),
                        nextStartTime
                );
            }
        }, startTime);
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
            return action;
        }
        return () -> {
            action.run();
            nextChainedTask.start(scheduler);
        };
    }

    private boolean isStarted() {
        return future != null;
    }

    private static class Builder {
        private Runnable action;
        private Duration preDelay = Duration.ZERO;
        private Duration postDelay = Duration.ZERO;

        public Builder action(Runnable action) {
            this.action = action;
            return this;
        }

        public Builder delayBefore(Duration delay) {
            this.preDelay = delay;
            return this;
        }

        public Builder delayAfter(Duration delay) {
            this.postDelay = delay;
            return this;
        }

        public ChainedTaskV2 build() {
            return new ChainedTaskV2(this);
        }
    }
}

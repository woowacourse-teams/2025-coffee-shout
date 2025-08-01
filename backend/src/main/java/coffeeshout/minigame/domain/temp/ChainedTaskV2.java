package coffeeshout.minigame.domain.temp;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.function.BooleanSupplier;
import org.springframework.scheduling.TaskScheduler;

public class ChainedTaskV2  {

    private final Runnable action;
    private final Duration preDelay;
    private final Duration postDelay;
    private ScheduledFuture<?> future;
    private ChainedTaskV2 previousChainedTask;
    private ChainedTaskV2 nextChainedTask;
    protected BooleanSupplier earlyCompletionCondition;
    protected ChainedTaskV2 earlyCompletionTask;

    private ChainedTaskV2(Builder builder) {
        this.action = builder.action;
        this.preDelay = builder.preDelay == null ? Duration.ZERO : builder.preDelay;
        this.postDelay = builder.postDelay == null ? Duration.ZERO : builder.postDelay;
    }

    public void start(final TaskScheduler scheduler) {
        final Instant startTime = getStartTime();

        future = scheduler.schedule(() -> {
            action.run();

            // 조건부 체이닝 체크
            if (earlyCompletionCondition != null && earlyCompletionCondition.getAsBoolean()) {
                if (earlyCompletionTask != null) {
                    earlyCompletionTask.start(scheduler);
                }
            } else if (nextChainedTask != null) {
                // 기존 체이닝 로직
                scheduler.schedule(() -> nextChainedTask.start(scheduler),
                        Instant.now().plus(postDelay));
            }
        }, startTime);
    }

    private Instant getStartTime() {
        if (previousChainedTask != null) {
            return Instant.now().plus(preDelay).plus(previousChainedTask.postDelay);
        }
        return Instant.now().plus(preDelay);
    }

    public void setNextTask(ChainedTaskV2 nextChainedTask) {
        this.nextChainedTask = nextChainedTask;
        nextChainedTask.previousChainedTask = this;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Runnable action;
        private Duration preDelay = Duration.ZERO;
        private Duration postDelay = Duration.ZERO;
        private BooleanSupplier earlyCompletionCondition;
        private ChainedTaskV2 earlyCompletionTask;

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

        public Builder chainOnCondition(BooleanSupplier condition, ChainedTaskV2 task) {
            this.earlyCompletionCondition = condition;
            this.earlyCompletionTask = task;
            return this;
        }

        public ChainedTaskV2 build() {
            ChainedTaskV2 task = new ChainedTaskV2(this);
            task.earlyCompletionCondition = this.earlyCompletionCondition;
            task.earlyCompletionTask = this.earlyCompletionTask;
            return task;
        }
    }
}

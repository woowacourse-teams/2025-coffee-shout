//package coffeeshout.minigame.domain.temp;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ScheduledFuture;
//import org.springframework.scheduling.TaskScheduler;
//
//public class ChainedTaskV2  {
//
//    private final Runnable action;
//    private final Duration preDelay;
//    private final Duration postDelay;
//    private ScheduledFuture<?> future;
//    private ChainedTaskV2 previousChainedTask;
//    private ChainedTaskV2 nextChainedTask;
//
//    private ChainedTaskV2(Builder builder) {
//        this.action = builder.action;
//        this.preDelay = builder.preDelay == null ? Duration.ZERO : builder.preDelay;
//        this.postDelay = builder.postDelay == null ? Duration.ZERO : builder.postDelay;
//    }
//
//    public void start(final TaskScheduler scheduler) {
//        final Instant startTime = getStartTime();
//
//        future = scheduler.schedule(() -> {
//            action.run();
//            if (nextChainedTask != null) {
//                nextChainedTask.start(scheduler);
//            }
//        }, startTime);
//    }
//
//    private Instant getStartTime() {
//        if (previousChainedTask != null) {
//            return Instant.now().plus(preDelay).plus(previousChainedTask.postDelay);
//        }
//        return Instant.now().plus(preDelay);
//    }
//
//    public void setNextTask(ChainedTaskV2 nextChainedTask) {
//        this.nextChainedTask = nextChainedTask;
//        nextChainedTask.previousChainedTask = this;
//    }
//
//    public void join() throws ExecutionException, InterruptedException {
//        this.future.get();
//        if (nextChainedTask != null) {
//            this.nextChainedTask.join();
//        }
//    }
//
//    public void cancel() {
//        if (!isStarted()) {
//            return;
//        }
//        future.cancel(false);
//    }
//
//    private Runnable chainedRunnable(TaskScheduler scheduler) {
//        if (nextChainedTask == null) {
//            return action;
//        }
//        return () -> {
//            action.run();
//            nextChainedTask.start(scheduler);
//        };
//    }
//
//    private boolean isStarted() {
//        return future != null;
//    }
//
//    public static Builder builder() {
//        return new Builder();
//    }
//
//    public static class Builder {
//        private Runnable action;
//        private Duration preDelay = Duration.ZERO;
//        private Duration postDelay = Duration.ZERO;
//
//        public Builder action(Runnable action) {
//            this.action = action;
//            return this;
//        }
//
//        public Builder delayBefore(Duration delay) {
//            this.preDelay = delay;
//            return this;
//        }
//
//        public Builder delayAfter(Duration delay) {
//            this.postDelay = delay;
//            return this;
//        }
//
//        public ChainedTaskV2 build() {
//            return new ChainedTaskV2(this);
//        }
//    }
//}

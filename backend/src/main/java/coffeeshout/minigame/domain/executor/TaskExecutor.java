package coffeeshout.minigame.domain.executor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.Getter;

@Getter
public class TaskExecutor<T> {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<T, Future<?>> futureTasks = new LinkedHashMap<>();

    public record Task<T>(T info, Runnable runnable) {
    }

    public void submits(List<Task<T>> tasks) {
        tasks.forEach(task -> {
            Future<?> future = executor.submit(task.runnable);
            futureTasks.put(task.info, future);
        });
    }

    public void cancel(T info) {
        final Future<?> future = futureTasks.get(info);
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
    }

    public void cancelAll() {
        futureTasks.values().forEach(future -> {
            if (!future.isDone()) {
                future.cancel(true);
            }
        });
    }
}

package coffeeshout.minigame.domain.temp;

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

    public record Task<T>(T info, Runnable task) {}

    public void submits(List<Task<T>> tasks) {
        tasks.forEach(task -> futureTasks.put(task.info, executor.submit(task.task)));
    }

    public void cancel(T info) {
        Future<?> future = futureTasks.get(info);
        if (future != null &&  !future.isDone()) {
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

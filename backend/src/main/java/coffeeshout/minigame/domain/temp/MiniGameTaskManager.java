package coffeeshout.minigame.domain.temp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;

@RequiredArgsConstructor
public class MiniGameTaskManager<T> {

    private final Map<T, ChainedTask> tasks = new ConcurrentHashMap<>();
    private final TaskScheduler scheduler;
    private ChainedTask lastTask;

    public void addTask(T type, ChainedTask task) {
        if (lastTask != null) {
            lastTask.setNextTask(task);
        }
        lastTask = task;
        tasks.put(type, task);
    }

    public void startWith(T type) {
        ChainedTask chainedTask = tasks.get(type);
        chainedTask.start(scheduler);
    }

    public void cancel(T type) {
        ChainedTask chainedTask = tasks.get(type);
        chainedTask.cancel();
    }

    public void join(T type) throws ExecutionException, InterruptedException {
        tasks.get(type).join();
    }
}

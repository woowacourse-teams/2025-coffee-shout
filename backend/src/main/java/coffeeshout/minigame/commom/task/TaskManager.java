package coffeeshout.minigame.commom.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.TaskScheduler;

public class TaskManager<T> {

    private final Map<T, ChainedTask> tasks;
    private final TaskScheduler scheduler;
    private ChainedTask lastTask;

    public TaskManager(TaskScheduler scheduler) {
        this.tasks = new ConcurrentHashMap<>();
        this.scheduler = scheduler;
    }

    public void addTask(T type, ChainedTask task) {
        if (lastTask != null) {
            lastTask.setNextTask(task);
        }
        lastTask = task;
        tasks.put(type, task);
    }

    public void startWith(T type) {
        final ChainedTask chainedTask = tasks.get(type);
        chainedTask.start(scheduler);
    }

    public void cancelAll() {
        tasks.values().forEach(ChainedTask::cancel);
    }
}

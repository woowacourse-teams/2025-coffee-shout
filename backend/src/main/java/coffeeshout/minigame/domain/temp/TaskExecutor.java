package coffeeshout.minigame.domain.temp;

import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutor.CardGameTask;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskExecutor {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<Task> tasks;

    private Future<?> currentFuture;
    private Task currentTask;

    public TaskExecutor(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void submits() {
        for (Task task : tasks) {
            this.currentTask = task;
            this.currentFuture = executor.submit(currentTask::run);
        }
    }

    public void cancelCurrentTask() {
        if (currentFuture != null && !currentFuture.isDone()) {
            currentFuture.cancel(true);
        }
    }
}

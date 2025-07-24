package coffeeshout.minigame.domain.cardgame;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.Getter;

@Getter
public class CardGameTaskExecutor {

    public record CardGameTask(CardGameState state, Runnable mainTask, Runnable postTask) {

        public void run() {
            try {
                mainTask.run();
                Thread.sleep(state.getDuration());
                postTask.run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<CardGameTask> cardGameTasks;
    private Future<?> currentTask;
    private CardGameTask currentCardGameTask;

    public CardGameTaskExecutor(List<CardGameTask> cardGameTasks) {
        this.cardGameTasks = cardGameTasks;
    }

    public void submits() {
        for (CardGameTask cardGameTask : cardGameTasks) {
            this.currentCardGameTask = cardGameTask;
            this.currentTask = executor.submit(cardGameTask::run);
        }
    }

    public void cancelPlaying() {
        if (currentCardGameTask.state == CardGameState.PLAYING) {
            cancelCurrentTask();
        }
    }

    private void cancelCurrentTask() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
    }
}

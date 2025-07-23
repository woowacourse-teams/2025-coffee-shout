package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.cardgame.CardGameState;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoomTaskExecutor {

    public record RoomTask(CardGameState state, Runnable task, Runnable postTask) {

        public void run() {
            try {
                task.run();
                log.info(state.name());
                postTask.run();
                Thread.sleep(state.getDuration());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<RoomTask> roomTasks;
    private Future<?> currentTask;
    private RoomTask currentRoomTask;

    public RoomTaskExecutor(List<RoomTask> roomTasks) {
        this.roomTasks = roomTasks;
    }

    public void submits() {
        for (RoomTask roomTask : roomTasks) {
            this.currentRoomTask = roomTask;
            this.currentTask = executor.submit(roomTask::run);
        }
    }

    public void cancelPlaying() {
        if (currentRoomTask.state == CardGameState.PLAYING) {
            cancelCurrentTask();
        }
    }

    private void cancelCurrentTask() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
    }
}

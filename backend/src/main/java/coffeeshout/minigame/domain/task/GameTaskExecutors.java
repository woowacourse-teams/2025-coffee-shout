package coffeeshout.minigame.domain.task;

import coffeeshout.room.domain.JoinCode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;


@Component
public class GameTaskExecutors<T> {
    private final Map<JoinCode, MiniGameTaskManager<T>> roomTaskExecutorMap;

    public GameTaskExecutors() {
        this.roomTaskExecutorMap = new HashMap<>();
    }

    public void put(JoinCode joinCode, MiniGameTaskManager<T> executor) {
        roomTaskExecutorMap.put(joinCode, executor);
    }

    public MiniGameTaskManager<T> get(JoinCode joinCode) {
        return roomTaskExecutorMap.get(joinCode);
    }

    public void cancelPlaying(JoinCode joinCode, T type) {
        roomTaskExecutorMap.get(joinCode).cancel(type);
    }
}

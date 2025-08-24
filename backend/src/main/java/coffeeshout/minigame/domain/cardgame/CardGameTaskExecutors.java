package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.common.task.TaskManager;
import coffeeshout.room.domain.JoinCode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class CardGameTaskExecutors {

    private final Map<JoinCode, TaskManager<CardGameTaskType>> roomTaskExecutorMap;

    public CardGameTaskExecutors() {
        this.roomTaskExecutorMap = new ConcurrentHashMap<>();
    }

    public void put(JoinCode joinCode, TaskManager<CardGameTaskType> executor) {
        roomTaskExecutorMap.put(joinCode, executor);
    }

    public TaskManager<CardGameTaskType> get(JoinCode joinCode) {
        return roomTaskExecutorMap.get(joinCode);
    }
}

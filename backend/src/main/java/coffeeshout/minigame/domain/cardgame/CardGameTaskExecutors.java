package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.task.CardGameTaskType;
import coffeeshout.minigame.domain.task.MiniGameTaskManager;
import coffeeshout.room.domain.JoinCode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CardGameTaskExecutors {

    private final Map<JoinCode, MiniGameTaskManager<CardGameTaskType>> roomTaskExecutorMap;

    public CardGameTaskExecutors() {
        this.roomTaskExecutorMap = new HashMap<>();
    }

    public void put(JoinCode joinCode, MiniGameTaskManager<CardGameTaskType> executor) {
        roomTaskExecutorMap.put(joinCode, executor);
    }

    public MiniGameTaskManager<CardGameTaskType> get(JoinCode joinCode) {
        return roomTaskExecutorMap.get(joinCode);
    }

    public void cancelPlaying(JoinCode joinCode, CardGameTaskType type) {
        roomTaskExecutorMap.get(joinCode).cancelDelay(type);
    }
}

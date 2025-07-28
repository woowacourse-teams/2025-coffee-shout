package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.temp.CardGameTaskInfo;
import coffeeshout.minigame.domain.temp.TaskExecutor;
import coffeeshout.room.domain.JoinCode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CardGameTaskExecutors {

    private final Map<JoinCode, TaskExecutor<CardGameTaskInfo>> roomTaskExecutorMap;

    public CardGameTaskExecutors() {
        this.roomTaskExecutorMap = new HashMap<>();
    }

    public void put(JoinCode joinCode, TaskExecutor<CardGameTaskInfo> executor) {
        roomTaskExecutorMap.put(joinCode, executor);
    }

    public TaskExecutor<CardGameTaskInfo> get(JoinCode joinCode) {
        return roomTaskExecutorMap.get(joinCode);
    }

    public void cancelPlaying(JoinCode joinCode, CardGameRound round) {
        roomTaskExecutorMap.get(joinCode).cancel(CardGameTaskInfo.getPlayingState(round));
    }
}

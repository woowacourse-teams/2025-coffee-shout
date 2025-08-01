package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.temp.CardGameTaskType;
import coffeeshout.minigame.domain.temp.MiniGameTaskManager;
import coffeeshout.room.domain.JoinCode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CardGameTaskExecutorsV2 {

    private final Map<JoinCode, MiniGameTaskManager<CardGameTaskType>> roomTaskExecutorMap;

    public CardGameTaskExecutorsV2() {
        this.roomTaskExecutorMap = new HashMap<>();
    }

    public void put(JoinCode joinCode, MiniGameTaskManager<CardGameTaskType> executor) {
        roomTaskExecutorMap.put(joinCode, executor);
    }

    public MiniGameTaskManager<CardGameTaskType> get(JoinCode joinCode) {
        return roomTaskExecutorMap.get(joinCode);
    }

    public void cancelPlaying(JoinCode joinCode, CardGameTaskType type) {
        roomTaskExecutorMap.get(joinCode).cancel(type);
    }

    // TODO 게임 완료 시에 해당 MiniGameTaskManager를 지워야 함
}

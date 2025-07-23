package coffeeshout.minigame.domain.cardgame;

import java.util.HashMap;
import java.util.Map;

public class CardGameTaskExecutors {

    private final Map<Long, CardGameTaskExecutor> roomTaskExecutorMap;

    public CardGameTaskExecutors() {
        this.roomTaskExecutorMap = new HashMap<>();
    }

    public void put(Long roomId, CardGameTaskExecutor executor) {
        roomTaskExecutorMap.put(roomId, executor);
    }

    public CardGameTaskExecutor get(Long roomId) {
        return roomTaskExecutorMap.get(roomId);
    }
}

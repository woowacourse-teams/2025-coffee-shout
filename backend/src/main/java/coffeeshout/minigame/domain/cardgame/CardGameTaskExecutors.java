package coffeeshout.minigame.domain.cardgame;

import coffeeshout.room.domain.JoinCode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CardGameTaskExecutors {

    private final Map<JoinCode, CardGameTaskExecutor> roomTaskExecutorMap;

    public CardGameTaskExecutors() {
        this.roomTaskExecutorMap = new HashMap<>();
    }

    public void put(JoinCode joinCode, CardGameTaskExecutor executor) {
        roomTaskExecutorMap.put(joinCode, executor);
    }

    public CardGameTaskExecutor get(JoinCode joinCode) {
        return roomTaskExecutorMap.get(joinCode);
    }
}

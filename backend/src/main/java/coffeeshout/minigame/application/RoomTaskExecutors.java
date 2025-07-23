package coffeeshout.minigame.application;

import java.util.HashMap;
import java.util.Map;

public class RoomTaskExecutors {

    private final Map<Long, RoomTaskExecutor> roomTaskExecutorMap;

    public RoomTaskExecutors() {
        this.roomTaskExecutorMap = new HashMap<>();
    }

    public void put(Long roomId, RoomTaskExecutor executor) {
        roomTaskExecutorMap.put(roomId, executor);
    }

    public RoomTaskExecutor get(Long roomId) {
        return roomTaskExecutorMap.get(roomId);
    }
}

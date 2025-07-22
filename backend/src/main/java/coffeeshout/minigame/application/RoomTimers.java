package coffeeshout.minigame.application;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class RoomTimers {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
    private final Map<Long, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();

    public void start(Long roomId, Runnable runnable, int milliSeconds) {
        timers.put(roomId, scheduledExecutorService.schedule(runnable, milliSeconds, TimeUnit.MILLISECONDS));
    }

    public void cancel(Long roomId) {
        if (!timers.containsKey(roomId)) {
            return;
        }
        timers.get(roomId).cancel(true);
    }
}

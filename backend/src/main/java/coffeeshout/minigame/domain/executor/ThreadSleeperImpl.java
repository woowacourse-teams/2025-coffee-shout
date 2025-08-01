package coffeeshout.minigame.domain.executor;

import org.springframework.stereotype.Component;

@Component
public class ThreadSleeperImpl implements ThreadSleeper {

    @Override
    public void sleep(long milliSecond) {
        try {
            Thread.sleep(milliSecond);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

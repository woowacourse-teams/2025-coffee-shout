package coffeeshout.minigame.racinggame.domain;

import coffeeshout.room.domain.player.Player;
import java.time.Duration;
import java.time.Instant;
import lombok.Getter;

@Getter
public class Runner {

    private static final int INITIAL_SPEED = 5;
    private static final int MIN_SPEED = 1;
    private static final int MAX_SPEED = 10;
    private static final int FINISH_LINE = 1000;
    private static final double CLICKS_PER_SECOND_THRESHOLD = 10.0;

    private final Player player;
    private int position = 0;
    private int speed = INITIAL_SPEED;
    private Instant lastSpeedUpdateTime;
    private Instant finishTime;

    public Runner(Player player) {
        this.player = player;
        this.lastSpeedUpdateTime = Instant.now();
    }

    public void adjustSpeed(int tapCount, Instant currentTime) {
        if (lastSpeedUpdateTime == null) {
            this.lastSpeedUpdateTime = currentTime;
            return;
        }

        long millisBetween = Duration.between(lastSpeedUpdateTime, currentTime).toMillis();
        if (millisBetween <= 0) {
            return;
        }

        double clicksPerSecond = (tapCount * 1000.0) / millisBetween;

        if (clicksPerSecond >= CLICKS_PER_SECOND_THRESHOLD) {
            this.speed = MAX_SPEED;
        } else {
            int newSpeed = (int) Math.ceil(clicksPerSecond);
            this.speed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, newSpeed));
        }

        this.lastSpeedUpdateTime = currentTime;
    }

    public void move() {
        if (isFinished()) {
            return;
        }

        this.position += speed;

        if (position >= FINISH_LINE) {
            this.position = FINISH_LINE;
            this.finishTime = Instant.now();
        }
    }

    public boolean isFinished() {
        return position >= FINISH_LINE;
    }
}

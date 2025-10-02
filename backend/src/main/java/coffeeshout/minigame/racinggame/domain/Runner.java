package coffeeshout.minigame.racinggame.domain;

import coffeeshout.room.domain.player.Player;
import java.time.Duration;
import java.time.Instant;
import lombok.Getter;

@Getter
public class Runner {

    private final Player player;
    private int position = 0;
    private int speed = RacingGame.INITIAL_SPEED;
    private Instant lastSpeedUpdateTime;
    private Instant finishTime;

    public Runner(Player player) {
        this.player = player;
        this.lastSpeedUpdateTime = Instant.now();
    }

    public void adjustSpeed(int tapCount) {
        final var now = Instant.now();
        if (lastSpeedUpdateTime == null) {
            this.lastSpeedUpdateTime = now;
            return;
        }

        final long duration = Duration.between(lastSpeedUpdateTime, now).toMillis();
        if (duration <= 0) {
            return;
        }

        final double clicksPerSecond = (tapCount * 1000.0) / duration;

        if (clicksPerSecond >= RacingGame.CLICKS_PER_SECOND_THRESHOLD) {
            this.speed = RacingGame.MAX_SPEED;
        } else {
            int newSpeed = (int) Math.ceil(clicksPerSecond);
            this.speed = Math.max(RacingGame.MIN_SPEED, Math.min(RacingGame.MAX_SPEED, newSpeed));
        }

        this.lastSpeedUpdateTime = now;
    }

    public void move() {
        if (isFinished()) {
            return;
        }

        this.position += speed;

        if (position >= RacingGame.FINISH_LINE) {
            this.position = RacingGame.FINISH_LINE;
            this.finishTime = Instant.now();
        }
    }

    public boolean isFinished() {
        return position >= RacingGame.FINISH_LINE;
    }
}

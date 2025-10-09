package coffeeshout.racinggame.domain;

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
        final Instant now = Instant.now();

        if (isFirstUpdate()) {
            this.lastSpeedUpdateTime = now;
            return;
        }

        final long elapsedMillis = calculateElapsedMillis(now);
        if (elapsedMillis <= 0) {
            return;
        }

        final double clicksPerSecond = calculateClicksPerSecond(tapCount, elapsedMillis);
        this.speed = determineSpeed(clicksPerSecond);
        this.lastSpeedUpdateTime = now;
    }

    private boolean isFirstUpdate() {
        return lastSpeedUpdateTime == null;
    }

    private long calculateElapsedMillis(Instant now) {
        return Duration.between(lastSpeedUpdateTime, now).toMillis();
    }

    private double calculateClicksPerSecond(int tapCount, long elapsedMillis) {
        return (tapCount * 1000.0) / elapsedMillis;
    }

    private int determineSpeed(double clicksPerSecond) {
        if (clicksPerSecond >= RacingGame.CLICKS_PER_SECOND_THRESHOLD) {
            return RacingGame.scaleMaxSpeed();
        }

        int calculatedSpeed = (int) Math.ceil(clicksPerSecond);
        return clampSpeed(calculatedSpeed);
    }

    private int clampSpeed(int speed) {
        return Math.max(RacingGame.scaleMinSpeed(), Math.min(RacingGame.scaleMaxSpeed(), speed));
    }

    public void move() {
        if (isFinished()) {
            return;
        }
        this.position += speed;
        if (position >= RacingGame.scaleFinishLine()) {
            this.position = RacingGame.scaleFinishLine();
            this.speed = RacingGame.INITIAL_SPEED;
            this.finishTime = Instant.now();
        }
    }

    public boolean isFinished() {
        return position >= RacingGame.scaleFinishLine();
    }

    public void firstMoveSpeed() {
        this.speed = RacingGame.scaleMinSpeed();
    }
}

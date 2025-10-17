package coffeeshout.racinggame.domain;

import static org.springframework.util.Assert.*;

import coffeeshout.room.domain.player.Player;
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

    public void updateSpeed(int tapCount, SpeedCalculator speedCalculator, Instant now) {
        if (isFinished()) {
            return;
        }
        final int nextSpeed = speedCalculator.calculateSpeed(lastSpeedUpdateTime, now, tapCount);
        isTrue(nextSpeed >= RacingGame.MIN_SPEED && nextSpeed <= RacingGame.MAX_SPEED, "스피드는 0 ~ 30이어야 합니다.");
        this.lastSpeedUpdateTime = now;
        this.speed = nextSpeed;
    }

    public void move(Instant now) {
        if (!isNotStopped()) {
            return;
        }
        final int nextPosition = position + speed;
        if (nextPosition >= RacingGame.FINISH_LINE && !isFinished()) {
            final long remainingDistance = speed - nextPosition % RacingGame.FINISH_LINE;
            final double millisPerPosition = RacingGame.MOVE_INTERVAL_MILLIS / (double) speed;
            final long remainingMillis = (long) (millisPerPosition * remainingDistance);
            finishTime = now.minusMillis(RacingGame.MOVE_INTERVAL_MILLIS).plusMillis(remainingMillis);
        }
        if (isFinished() && isNotStopped()) {
            slowDown();
        }
        this.position = nextPosition;
    }

    private void slowDown() {
        if (speed - 3 <= 0) {
            speed = 0;
            return;
        }
        speed -= 3;
    }

    public boolean isFinished() {
        return position >= RacingGame.FINISH_LINE;
    }

    public void initializeSpeed() {
        this.speed = RacingGame.MIN_SPEED;
    }

    public void initializeLastSpeedUpdateTime(Instant time) {
        this.lastSpeedUpdateTime = time;
    }

    public boolean isNotStopped() {
        return speed != 0;
    }
}

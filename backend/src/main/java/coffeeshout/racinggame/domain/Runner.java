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
        final int nextSpeed = speedCalculator.calculateSpeed(lastSpeedUpdateTime, now, tapCount);
        isTrue(nextSpeed >= RacingGame.MIN_SPEED && nextSpeed <= RacingGame.MAX_SPEED, "스피드는 0 ~ 30이어야 합니다.");
        this.lastSpeedUpdateTime = now;
        this.speed = nextSpeed;
    }

    public void move() {
        if (isFinished()) {
            return;
        }
        this.position += speed;
        if (position >= RacingGame.FINISH_LINE) {
            this.speed = RacingGame.INITIAL_SPEED;
            this.finishTime = Instant.now();
        }
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
}

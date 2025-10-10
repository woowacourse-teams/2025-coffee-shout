package coffeeshout.racinggame.domain;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class TapPerSecondSpeedCalculator implements SpeedCalculator {

    @Override
    public int calculateSpeed(Instant lastTapedTime, Instant now, int tapCount) {
        final int boundedClickCount = Math.min(tapCount, RacingGame.CLICK_COUNT_THRESHOLD);
        final Duration duration = Duration.between(lastTapedTime, now);
        return convertToSpeed(calculateClickPerSecond(boundedClickCount, duration));
    }

    private double calculateClickPerSecond(int boundedClickCount, Duration duration) {
        return (double) boundedClickCount / duration.toMillis() * 1000;
    }

    private int convertToSpeed(double clicksPerSecond) {
        double speed = (clicksPerSecond / RacingGame.CLICK_COUNT_THRESHOLD) * RacingGame.MAX_SPEED;
        return (int) Math.min(Math.max(speed, RacingGame.MIN_SPEED), RacingGame.MAX_SPEED);
    }
}

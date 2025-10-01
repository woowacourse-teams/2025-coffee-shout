package coffeeshout.minigame.racinggame.domain;

import coffeeshout.room.domain.player.Player;
import lombok.Getter;

@Getter
public class Runner {

    private static final int INITIAL_SPEED = 5;
    private static final int MIN_SPEED = 5;
    private static final int MAX_SPEED = 100;
    private static final int FINISH_LINE = 1000;

    private final Player player;
    private int position = 0;
    private int speed = INITIAL_SPEED;

    public Runner(Player player) {
        this.player = player;
    }

    public void adjustSpeed(int adjustment) {
        int newSpeed = speed + adjustment;
        if (newSpeed >= MAX_SPEED || newSpeed <= MIN_SPEED) {
            throw new IllegalArgumentException("속도는 0 ~ 100 사이여야 합니다.");
        }
        this.speed = newSpeed;
    }

    public void move() {
        if (isFinished()) {
            throw new IllegalArgumentException("이미 완주하여 더이상 움직일 수 없습니다.");
        }
        int newPosition = position + speed;
        this.position = Math.min(newPosition, FINISH_LINE);
    }

    public void move(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("뒤로는 움직일 수 없습니다.");
        }
        if (position + offset >= FINISH_LINE) {
            throw new IllegalArgumentException("이미 완주하여 더이상 움직일 수 없습니다.");
        }
        this.position += offset;
    }

    public boolean isFinished() {
        return position >= FINISH_LINE;
    }
}

package coffeeshout.room.domain.player;

import static org.springframework.util.Assert.isTrue;

public record PlayerName(String value) {

    private static final int MAX_NAME_LENGTH = 10;

    public PlayerName {
        isTrue(!value.isBlank(), "이름은 공백일 수 없습니다.");
        isTrue(value.length() <= MAX_NAME_LENGTH, "이름은 10자 이하여야 합니다.");
    }

    public static PlayerName from(String name) {
        return new PlayerName(name);
    }
}

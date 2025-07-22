package coffeeshout.room.domain.player;

import org.springframework.util.Assert;

public record PlayerName(String value) {

    public PlayerName {
        Assert.isTrue(!value.isBlank(), "이름은 공백일 수 없습니다.");
    }

    public static PlayerName from(String name) {
        return new PlayerName(name);
    }
}

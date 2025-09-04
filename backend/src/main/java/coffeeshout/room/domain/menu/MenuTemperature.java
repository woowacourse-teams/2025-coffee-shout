package coffeeshout.room.domain.menu;

import java.util.Arrays;

public enum MenuTemperature {
    HOT,
    ICE,
    ;

    public static MenuTemperature from(String temperature) {
        return Arrays.stream(values()).filter(it -> it.name().equals(temperature))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴 온도입니다."));
    }
}

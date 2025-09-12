package coffeeshout.domain;

import lombok.Getter;

@Getter
public final class JoinCode {

    private final String value;

    public JoinCode(String value) {
        this.value = value;
    }
}

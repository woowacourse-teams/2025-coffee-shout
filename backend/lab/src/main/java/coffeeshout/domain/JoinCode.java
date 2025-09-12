package coffeeshout.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public final class JoinCode {

    private final String value;

    public JoinCode(String value) {
        this.value = value;
    }
}

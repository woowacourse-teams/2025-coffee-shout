package coffeeshout.domain;

import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpecialCard implements Card {
    private final Integer value;

    @Override
    public CardType getType() {
        return CardType.SPECIAL;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpecialCard that = (SpecialCard) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}

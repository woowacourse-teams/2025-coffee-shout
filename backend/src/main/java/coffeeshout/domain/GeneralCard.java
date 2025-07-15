package coffeeshout.domain;

import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GeneralCard implements Card {

    private final Integer value;

    @Override
    public CardType getType() {
        return CardType.GENERAL;
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
        GeneralCard that = (GeneralCard) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}

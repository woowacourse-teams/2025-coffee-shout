package coffeeshout.minigame.domain.cardgame.card;

import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public abstract class Card {

    private CardType type;
    private int value;

    protected Card(CardType type, int value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Card card = (Card) o;
        return value == card.value && type == card.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}

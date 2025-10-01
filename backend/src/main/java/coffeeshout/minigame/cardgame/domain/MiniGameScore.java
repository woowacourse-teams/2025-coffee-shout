package coffeeshout.minigame.cardgame.domain;

import coffeeshout.minigame.cardgame.domain.cardgame.CardGameScore;
import java.util.Objects;

public abstract class MiniGameScore implements Comparable<MiniGameScore> {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final CardGameScore that)) {
            return false;
        }
        return Objects.equals(this.getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    @Override
    public int compareTo(final MiniGameScore o) {
        return Integer.compare(this.getValue(), o.getValue());
    }

    public abstract int getValue();
}

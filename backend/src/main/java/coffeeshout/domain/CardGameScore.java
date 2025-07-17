package coffeeshout.domain;

import java.util.Objects;

public class CardGameScore implements Comparable<CardGameScore> {

    private int addition;
    private int multiplier;

    public CardGameScore() {
        this.addition = 0;
        this.multiplier = 1;
    }

    public void addCard(Card card) {
        if (card.getType() == CardType.ADDITION) {
            addition += card.value();
        }

        if (card.getType() == CardType.MULTIPLIER) {
            multiplier *= card.value();
        }
    }

    public int getResult() {
        return addition * multiplier;
    }

    @Override
    public int compareTo(final CardGameScore o) {
        return Integer.compare(this.getResult(), o.getResult());
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final CardGameScore that)) {
            return false;
        }
        return Objects.equals(this.getResult(), that.getResult());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getResult());
    }
}



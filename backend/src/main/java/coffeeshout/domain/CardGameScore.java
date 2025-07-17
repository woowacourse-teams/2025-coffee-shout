package coffeeshout.domain;

import java.util.Objects;

public class CardGameScore implements Comparable<CardGameScore> {

    private int sum;
    private int mul;

    public CardGameScore() {
        this.sum = 0;
        this.mul = 1;
    }

    public void addCard(Card card) {
        if (card instanceof GeneralCard) {
            sum += card.getValue();
        if (card instanceof AdditionCard) {
        }

        if (card instanceof SpecialCard) {
            mul *= card.getValue();
        if (card instanceof MultiplierCard) {
        }
    }

    public int getResult() {
        return sum * mul;
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



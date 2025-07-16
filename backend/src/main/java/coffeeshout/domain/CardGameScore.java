package coffeeshout.domain;

public class CardGameScore implements Comparable<CardGameScore> {

    private Integer sum;
    private Integer mul;

    public CardGameScore() {
        this.sum = 0;
        this.mul = 1;
    }

    public void addCard(Card card) {
        if (card instanceof GeneralCard) {
            sum += card.getValue();
        }

        if (card instanceof SpecialCard) {
            mul *= card.getValue();
        }
    }

    public Integer getResult() {
        return sum * mul;
    }

    @Override
    public int compareTo(final CardGameScore o) {
        return Integer.compare(this.getResult(), o.getResult());
    }
}



package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.CardType;
import java.util.Objects;

public class CardGameScore implements Comparable<CardGameScore> {

    public final static CardGameScore INF = new CardGameScore(Integer.MAX_VALUE);
    private int addition;
    private int multiplier;

    public CardGameScore() {
        this.addition = 0;
        this.multiplier = 1;
    }

    public CardGameScore(int addition) {
        this.addition = addition;
    }

    public CardGameScore(CardHand hand) {
        this.addition = 0;
        this.multiplier = 1;
        hand.forEach(this::updateScore);
    }

    public void updateScore(Card card) {
        if (card.getType() == CardType.ADDITION) {
            addition += card.getValue();
        }

        if (card.getType() == CardType.MULTIPLIER) {
            multiplier *= card.getValue();
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



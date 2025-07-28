package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.CardType;

public class CardGameScore extends MiniGameScore {

    public static final CardGameScore INF = new CardGameScore(Integer.MAX_VALUE);
    private int addition;
    private int multiplier;

    public CardGameScore(int addition) {
        this.addition = addition;
        this.multiplier = 1;
    }

    public CardGameScore(CardHand hand) {
        this.addition = 0;
        this.multiplier = 1;
        hand.forEach(this::updateScore);
    }

    private void updateScore(Card card) {
        if (card.getType() == CardType.ADDITION) {
            addition += card.getValue();
        }

        if (card.getType() == CardType.MULTIPLIER) {
            multiplier *= card.getValue();
        }
    }

    @Override
    public int getValue() {
        return addition * multiplier;
    }
}



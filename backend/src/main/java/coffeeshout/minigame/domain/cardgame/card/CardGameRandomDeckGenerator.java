package coffeeshout.minigame.domain.cardgame.card;

import java.util.List;

public class CardGameRandomDeckGenerator implements CardGameDeckGenerator {

    private static final AdditionCards ADDITION_CARDS = new AdditionCards();
    private static final MultiplierCards MULTIPLIER_CARDS = new MultiplierCards();

    @Override
    public Deck generate(int additionCardCount, int multiplierCardCount) {
        final List<Card> additionCards = ADDITION_CARDS.pickCards(additionCardCount);
        final List<Card> multiplierCards = MULTIPLIER_CARDS.pickCards(multiplierCardCount);
        final Deck deck = new Deck(additionCards, multiplierCards);
        deck.shuffle();
        return deck;
    }
}

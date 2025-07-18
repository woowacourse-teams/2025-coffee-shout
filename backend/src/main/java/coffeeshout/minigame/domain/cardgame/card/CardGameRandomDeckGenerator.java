package coffeeshout.minigame.domain.cardgame.card;

import java.util.List;

public class CardGameRandomDeckGenerator implements CardGameDeckGenerator {

    private static final AdditionCards ADDITION_CARDS = new AdditionCards();
    private static final MultiplierCards MULTIPLIER_CARDS = new MultiplierCards();

    private static final int ADDITION_CARD_COUNT = 6;
    private static final int MULTIPLIER_CARD_COUNT = 3;

    @Override
    public Deck generate() {
        final List<Card> additionCards = ADDITION_CARDS.pickCards(ADDITION_CARD_COUNT);
        final List<Card> multiplierCards = MULTIPLIER_CARDS.pickCards(MULTIPLIER_CARD_COUNT);
        final Deck deck = new Deck(additionCards, multiplierCards);
        deck.shuffle();
        return deck;
    }
}

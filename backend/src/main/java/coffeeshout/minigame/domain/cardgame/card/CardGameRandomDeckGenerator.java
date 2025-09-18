package coffeeshout.minigame.domain.cardgame.card;

import java.util.List;

public class CardGameRandomDeckGenerator implements CardGameDeckGenerator {


    @Override
    public Deck generate(int additionCardCount, int multiplierCardCount) {
        final List<Card> additionCards = GameCard.getRandomAdditionCards(additionCardCount);
        final List<Card> multiplierCards = GameCard.getRandomMultiplyCards(multiplierCardCount);
        final Deck deck = new Deck(additionCards, multiplierCards);
        // TODO  Generator에서 섞는게 이상하다 ?????
        deck.shuffle();
        return deck;
    }
}

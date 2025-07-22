package coffeeshout.fixture;

import coffeeshout.minigame.domain.cardgame.card.AdditionCard;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.MultiplierCard;
import coffeeshout.minigame.domain.cardgame.card.Deck;
import java.util.List;

public final class CardGameDeckStub implements CardGameDeckGenerator {

    private int count = 0;

    @Override
    public Deck generate(int additionCardCount, int multiplierCardCount) {
        List<Card> additionCards = List.of(
                new AdditionCard(40),
                new AdditionCard(30),
                new AdditionCard(20),
                new AdditionCard(10),
                new AdditionCard(0),
                new AdditionCard(-10));
        List<Card> multiplierCards = List.of(
                new MultiplierCard(4),
                new MultiplierCard(2),
                new MultiplierCard(0));
        count++;
        return new Deck(additionCards, multiplierCards);
    }
}

package coffeeshout.fixture;

import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.CardType;
import coffeeshout.minigame.domain.cardgame.card.Deck;
import java.util.List;

public final class CardGameDeckStub implements CardGameDeckGenerator {

    @Override
    public Deck generate(int additionCardCount, int multiplierCardCount) {
        List<Card> additionCards = List.of(
                new Card(CardType.ADDITION, 40),
                new Card(CardType.ADDITION, 30),
                new Card(CardType.ADDITION, 20),
                new Card(CardType.ADDITION, 10),
                new Card(CardType.ADDITION, 0),
                new Card(CardType.ADDITION, -10),
                new Card(CardType.ADDITION, -20)
        );
        List<Card> multiplierCards = List.of(
                new Card(CardType.MULTIPLIER, 4),
                new Card(CardType.MULTIPLIER, 2)
        );
        return new StubDeck(additionCards, multiplierCards);
    }
}

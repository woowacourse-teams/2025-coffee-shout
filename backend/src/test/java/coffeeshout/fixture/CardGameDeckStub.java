package coffeeshout.fixture;

import coffeeshout.minigame.domain.cardgame.AdditionCard;
import coffeeshout.minigame.domain.cardgame.Card;
import coffeeshout.minigame.domain.cardgame.CardGameDeckGenerator;
import coffeeshout.minigame.domain.cardgame.MultiplierCard;
import java.util.List;

public final class CardGameDeckStub implements CardGameDeckGenerator {

    private int count = 0;

    @Override
    public List<Card> spreadCards() {
        count++;
        return List.of(
                new AdditionCard(40),
                new AdditionCard(30),
                new AdditionCard(20),
                new AdditionCard(10),
                new AdditionCard(0),
                new AdditionCard(-10),
                new MultiplierCard(4),
                new MultiplierCard(2),
                new MultiplierCard(0)
        );
    }
}

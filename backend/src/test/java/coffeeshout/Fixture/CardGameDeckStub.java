package coffeeshout.Fixture;

import coffeeshout.domain.Card;
import coffeeshout.domain.CardGameDeckGenerator;
import coffeeshout.domain.GeneralCard;
import coffeeshout.domain.SpecialCard;
import java.util.List;

public class CardGameDeckStub implements CardGameDeckGenerator {

    private int count = 0;

    @Override
    public List<Card> spreadCards() {
        count++;
        return List.of(
                new GeneralCard(40),
                new GeneralCard(30),
                new GeneralCard(20),
                new GeneralCard(10),
                new GeneralCard(0),
                new GeneralCard(-10),
                new SpecialCard(4),
                new SpecialCard(2),
                new SpecialCard(0)
        );
    }
}

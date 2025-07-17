package coffeeshout.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardGameRandomDeckGenerator implements CardGameDeckGenerator {

    private static final List<Card> GENERAL_CARDS = List.of(
            new AdditionCard(-40),
            new AdditionCard(-30),
            new AdditionCard(-20),
            new AdditionCard(-10),
            new AdditionCard(0),
            new AdditionCard(10),
            new AdditionCard(20),
            new AdditionCard(30),
            new AdditionCard(40)
    );

    private static final List<Card> SPECIAL_CARDS = List.of(
            new MultiplierCard(4),
            new MultiplierCard(2),
            new MultiplierCard(0),
            new MultiplierCard(-1)
    );

    @Override
    public List<Card> spreadCards() {
        final List<Card> generalCopy = new ArrayList<>(GENERAL_CARDS);
        final List<Card> specialCopy = new ArrayList<>(SPECIAL_CARDS);

        Collections.shuffle(generalCopy);
        Collections.shuffle(specialCopy);

        final List<Card> selectedCards = new ArrayList<>();
        selectedCards.addAll(generalCopy.subList(0, 6));
        selectedCards.addAll(specialCopy.subList(0, 3));

        Collections.shuffle(selectedCards);

        return selectedCards;
    }
}

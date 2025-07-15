package coffeeshout.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardGameDeck {

    private static final List<Card> GENERAL_CARDS = List.of(
            new GeneralCard(-40),
            new GeneralCard(-30),
            new GeneralCard(-20),
            new GeneralCard(-10),
            new GeneralCard(0),
            new GeneralCard(10),
            new GeneralCard(20),
            new GeneralCard(30),
            new GeneralCard(40)
    );

    private static final List<Card> SPECIAL_CARDS = List.of(
            new SpecialCard(4),
            new SpecialCard(2),
            new SpecialCard(0),
            new SpecialCard(-1)
    );

    public static List<Card> spreadCards() {
        List<Card> generalCopy = new ArrayList<>(GENERAL_CARDS);
        List<Card> specialCopy = new ArrayList<>(SPECIAL_CARDS);

        Collections.shuffle(generalCopy);
        Collections.shuffle(specialCopy);

        List<Card> selectedCards = new ArrayList<>();
        selectedCards.addAll(generalCopy.subList(0, 6));
        selectedCards.addAll(specialCopy.subList(0, 3));

        Collections.shuffle(selectedCards);

        return selectedCards;
    }
}

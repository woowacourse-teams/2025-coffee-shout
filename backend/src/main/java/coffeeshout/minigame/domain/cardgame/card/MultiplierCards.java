package coffeeshout.minigame.domain.cardgame.card;

import static coffeeshout.minigame.domain.cardgame.card.MultiplierCard.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiplierCards {

    private final List<Card> cards;

    public MultiplierCards() {
        this.cards = new ArrayList<>();
        this.cards.add(QUADRUPLE);
        this.cards.add(DOUBLE);
        this.cards.add(NULLIFY);
        this.cards.add(INVERT);
    }

    public List<Card> pickCards(int count) {
        Collections.shuffle(cards);
        return cards.subList(0, count);
    }
}

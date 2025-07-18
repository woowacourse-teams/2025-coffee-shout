package coffeeshout.minigame.domain.cardgame.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.NonNull;

public class Deck {

    private final List<Card> cards;

    public Deck(@NonNull List<Card> additionCards, @NonNull List<Card> multiplierCards) {
        this.cards = new ArrayList<>();
        this.cards.addAll(additionCards);
        this.cards.addAll(multiplierCards);
    }

    public  void shuffle() {
        Collections.shuffle(cards);
    }

    public Card pick(int cardIndex) {
        return cards.get(cardIndex);
    }

    public Stream<Card> stream() {
        return cards.stream();
    }

    public int size() {
        return cards.size();
    }
}

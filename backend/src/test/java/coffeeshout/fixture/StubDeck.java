package coffeeshout.fixture;

import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.Deck;
import io.micrometer.common.lang.NonNull;
import java.util.List;

public class StubDeck extends Deck {

    public StubDeck() {
    }

    public StubDeck(
            @NonNull List<Card> additionCards,
            @NonNull List<Card> multiplierCards
    ) {
        super(additionCards, multiplierCards);
    }

    @Override
    public void shuffle() {
    }
}

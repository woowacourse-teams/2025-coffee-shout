package coffeeshout.fixture;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;

public class CardGameFake extends CardGame {

    private String dummy = "dummy";

    public CardGameFake() {}

    public CardGameFake(CardGameDeckGenerator deckGenerator) {
        super(deckGenerator);
    }
}

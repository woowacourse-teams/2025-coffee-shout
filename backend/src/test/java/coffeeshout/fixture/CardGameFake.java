package coffeeshout.fixture;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.player.domain.Player;
import java.util.List;

public class CardGameFake extends CardGame {

    public CardGameFake(CardGameDeckGenerator deckGenerator, List<Player> players) {
        super(deckGenerator, players);
    }
}

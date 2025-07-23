package coffeeshout.fixture;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.card.Deck;
import coffeeshout.player.domain.Player;
import java.util.List;

public class CardGameFake extends CardGame {


    public CardGameFake(Deck deck, List<Player> players) {
        super(deck, players);
    }

}

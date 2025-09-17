package coffeeshout.fixture;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.player.Player;
import java.util.List;

public class CardGameFake extends CardGame {

    public CardGameFake() {}

    public CardGameFake(List<Player> players, JoinCode joinCode, CardGameDeckGenerator deckGenerator) {
        super(players, joinCode, deckGenerator);
    }
}

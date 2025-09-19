package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerHand {

    private Player player;
    private CardHand cardHand;

    public PlayerHand(Player player) {
        this.player = player;
        this.cardHand = new CardHand();
    }

    public int handSize() {
        return cardHand.size();
    }

    public void putCard(Card card) {
        cardHand.put(card);
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isSameName(PlayerName playerName) {
        return player.sameName(playerName);
    }

    public CardGameScore calculateScore() {
        return cardHand.calculateCardGameScore();
    }

    public boolean isSelected(CardGameRound round) {
        return cardHand.isSelected(round);
    }

    public boolean isAssigned(Card card, CardGameRound round) {
        return cardHand.isAssign(card, round);
    }
}

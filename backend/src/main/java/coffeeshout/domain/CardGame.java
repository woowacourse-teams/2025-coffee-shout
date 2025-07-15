package coffeeshout.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;

@Getter
public class CardGame implements Playable {

    private final Map<Player, List<Card>> playerCards;
    private final List<Card> cards;
    private final Integer round;

    public CardGame(List<Player> players) {
        this.playerCards = initPlayerCards(players);
        this.round = 1;
        this.cards = CardGameDeck.spreadCards();
    }

    @Override
    public void play() {

    }

    private Map<Player, List<Card>> initPlayerCards(List<Player> players) {
        Map<Player, List<Card>> playerCards = new HashMap<>();
        for (Player player : players) {
            playerCards.put(player, new ArrayList<>());
        }
        return playerCards;
    }

    public void selectCard(Player player, Integer cardPosition) {
        playerCards.get(player).add(cards.get(cardPosition));
    }

    public Map<Player, Integer> calculateScores() {
        Map<Player, Integer> scores = new HashMap<>();

        for (Entry<Player, List<Card>> playerCardEntry : playerCards.entrySet()) {
            scores.put(playerCardEntry.getKey(), sumCards(playerCardEntry.getValue()));
        }

        return scores;
    }

    private Integer sumCards(List<Card> cards) {
        Integer sum = 0;

        for (Card card : cards) {
            if (card instanceof GeneralCard) {
                sum += card.getValue();
            }
        }
        for (Card card : cards) {
            if (card instanceof SpecialCard) {
                sum *= card.getValue();
            }
        }
        return sum;
    }

    public Player findCardHolder(Card card, Integer round) {
        for (Entry<Player, List<Card>> playerCardsEntry : playerCards.entrySet()) {
            if (playerCardsEntry.getValue().get(round).equals(card)) {
                return playerCardsEntry.getKey();
            }
        }
        return null;
    }

    public Boolean allSelected() {
        Boolean allSelected = true;

        for (List<Card> cards : playerCards.values()) {
            if (cards.size() != 1) {
                return false;
            }
        }

        return allSelected;
    }
}

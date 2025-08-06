package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.cardgame.card.Card;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CardHand {

    private final List<Card> hand;

    public CardHand() {
        this.hand = new ArrayList<>();
    }

    public CardGameScore calculateCardGameScore() {
        return new CardGameScore(this);
    }

    public void put(Card card) {
        hand.add(card);
    }

    public int size() {
        return hand.size();
    }

    public void forEach(Consumer<Card> consumer) {
        hand.forEach(consumer);
    }

    public Card getCard(int index) {
        return hand.get(index);
    }
    
    // === 새로운 라운드 관리를 위한 메서드들 ===
    
    /**
     * 특정 라운드에서 카드를 선택했는지 확인 (1-based round number)
     */
    public boolean hasCardForRound(int roundNumber) {
        return roundNumber <= hand.size();
    }
    
    /**
     * 특정 라운드의 카드 반환 (1-based round number)
     */
    public Card getCardForRound(int roundNumber) {
        if (roundNumber <= 0 || roundNumber > hand.size()) {
            throw new IllegalArgumentException("잘못된 라운드 번호: " + roundNumber);
        }
        return hand.get(roundNumber - 1);
    }
}

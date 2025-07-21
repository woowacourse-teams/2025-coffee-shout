package coffeeshout.minigame.domain.cardgame.card;

import static org.springframework.util.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.NonNull;
import org.springframework.util.Assert;

public class Deck {

    private final List<Card> cards;
    private final List<Card> pickedCards;
    // 어떤 플레이어가 뽑았는지 저장
    // Map<Card, Player>

    public Deck(@NonNull List<Card> additionCards, @NonNull List<Card> multiplierCards) {
        this.cards = new ArrayList<>();
        this.pickedCards = new ArrayList<>();
        this.cards.addAll(additionCards);
        this.cards.addAll(multiplierCards);
    }

    public  void shuffle() {
        Collections.shuffle(cards);
    }

    public Card pick(int cardIndex) {
        Card pickedCard = cards.get(cardIndex);
        state(!isPicked(pickedCard), "이미 뽑은 카드입니다.");
        pickedCards.add(pickedCard);
        return pickedCard;
    }

    public Card pickRandom(){
        List<Card> cloned = new ArrayList<>(cards);
        cloned.removeAll(pickedCards);
        Collections.shuffle(cloned);
        Card pickedCard = cloned.getFirst();
        return pick(cards.indexOf(pickedCard));
    }

    public boolean isPicked(Card card){
        return pickedCards.contains(card);
    }

    public Stream<Card> stream() {
        return cards.stream();
    }

    public int size() {
        return cards.size();
    }
}

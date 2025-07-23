package coffeeshout.minigame.domain.cardgame.card;

import static org.springframework.util.Assert.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Deck {

    private final List<Card> cards;
    private final List<Card> pickedCards;

    public Deck(@NonNull List<Card> additionCards, @NonNull List<Card> multiplierCards) {
        this.cards = new ArrayList<>();
        this.pickedCards = new ArrayList<>();
        this.cards.addAll(additionCards);
        this.cards.addAll(multiplierCards);
    }

    public void shuffle() {
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

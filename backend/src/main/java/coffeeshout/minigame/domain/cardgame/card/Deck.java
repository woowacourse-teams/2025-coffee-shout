package coffeeshout.minigame.domain.cardgame.card;

import static org.springframework.util.Assert.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
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
        pickedCards.clear();
    }

    public Card pick(int cardIndex) {
        Card selectedCard = cards.get(cardIndex);
        state(!isPicked(selectedCard), "이미 뽑은 카드입니다.");
        pickedCards.add(selectedCard);
        return selectedCard;
    }

    public Card pickRandom() {
        List<Card> remainingCards = getRemainingCards();
        Card selected = pickRandom(remainingCards);
        return pick(cards.indexOf(selected));
    }

    private boolean isPicked(Card card) {
        return pickedCards.contains(card);
    }

    public Stream<Card> stream() {
        return cards.stream();
    }

    public int size() {
        return cards.size();
    }

    private List<Card> getRemainingCards() {
        List<Card> cloned = new ArrayList<>(cards);
        cloned.removeAll(pickedCards);
        return cloned;
    }

    private Card pickRandom(List<Card> cards) {
        int randomNumber = ThreadLocalRandom.current().nextInt(0, cards.size());
        return cards.get(randomNumber);
    }
}

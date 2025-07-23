package coffeeshout.minigame.domain.cardgame.card;

import static coffeeshout.minigame.domain.cardgame.card.AdditionCard.*;
import static org.springframework.util.Assert.isTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdditionCards {

    private final List<Card> cards;

    public AdditionCards() {
        this.cards = new ArrayList<>();
        this.cards.add(PLUS_40);
        this.cards.add(PLUS_30);
        this.cards.add(PLUS_20);
        this.cards.add(PLUS_10);
        this.cards.add(ZERO);
        this.cards.add(MINUS_10);
        this.cards.add(MINUS_20);
        this.cards.add(MINUS_30);
        this.cards.add(MINUS_40);
    }

    public List<Card> pickCards(int count) {
        isTrue(count <= cards.size(), "최대 사용 가능한 카드 수를 초과했습니다. size = " + cards.size());
        Collections.shuffle(cards);
        return cards.subList(0, count);
    }
}

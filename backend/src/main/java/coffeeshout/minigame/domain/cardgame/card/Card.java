package coffeeshout.minigame.domain.cardgame.card;

import lombok.Getter;

@Getter
public abstract class Card {

    private final CardType type;
    private final int value;

    protected Card(CardType type, int value) {
        this.type = type;
        this.value = value;
    }
}

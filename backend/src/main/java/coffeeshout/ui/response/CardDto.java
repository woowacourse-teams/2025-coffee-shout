package coffeeshout.ui.response;

import coffeeshout.domain.Card;
import coffeeshout.domain.CardType;

public record CardDto(
        CardType type,
        Integer value
) {

    public static CardDto from(Card card) {
        return new CardDto(card.getType(), card.getValue());
    }
}

package coffeeshout.minigame.domain.cardgame.card;

import static org.springframework.util.Assert.isTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum GameCard {
    PLUS_40(CardType.ADDITION, 40),
    PLUS_35(CardType.ADDITION, 35),
    PLUS_30(CardType.ADDITION, 30),
    PLUS_25(CardType.ADDITION, 25),
    PLUS_20(CardType.ADDITION, 20),
    PLUS_15(CardType.ADDITION, 15),
    PLUS_10(CardType.ADDITION, 10),
    PLUS_5(CardType.ADDITION, 5),
    ZERO(CardType.ADDITION, 0),
    MINUS_5(CardType.ADDITION, -5),
    MINUS_10(CardType.ADDITION, -10),
    MINUS_15(CardType.ADDITION, -15),
    MINUS_20(CardType.ADDITION, -20),
    MINUS_25(CardType.ADDITION, -25),
    MINUS_30(CardType.ADDITION, -30),
    MINUS_35(CardType.ADDITION, -35),
    MINUS_40(CardType.ADDITION, -40),

    QUADRUPLE(CardType.MULTIPLIER, 4),
    DOUBLE(CardType.MULTIPLIER, 2),
    INVERT(CardType.MULTIPLIER, -1);

    private final Card card;

    GameCard(CardType type, int value) {
        this.card = new Card(type, value);
    }

    public static List<Card> getRandomAdditionCards(int count) {
        final List<Card> additionCards = getCardsByType(CardType.ADDITION);
        return pickRandomCards(additionCards, count);
    }

    public static List<Card> getRandomMultiplyCards(int count) {
        final List<Card> multiplyCards = getCardsByType(CardType.MULTIPLIER);
        return pickRandomCards(multiplyCards, count);
    }

    private static List<Card> getCardsByType(CardType cardType) {
        return Arrays.stream(values())
                .map(GameCard::toCard)
                .filter(card -> card.getType() == cardType)
                .collect(Collectors.toList());
    }

    private static List<Card> pickRandomCards(List<Card> cards, int count) {
        isTrue(count <= cards.size(), "최대 사용 가능한 카드 수를 초과했습니다. size = " + cards.size());
        Collections.shuffle(cards);
        return cards.subList(0, count);
    }

    public Card toCard() {
        return this.card;
    }
}

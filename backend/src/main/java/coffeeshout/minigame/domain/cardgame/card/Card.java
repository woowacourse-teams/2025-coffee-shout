package coffeeshout.minigame.domain.cardgame.card;

public record Card(CardType type, int value) {

    public CardType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
}

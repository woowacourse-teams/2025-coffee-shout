package coffeeshout.domain;

public record MultiplierCard(
        Integer value
) implements Card {

    @Override
    public CardType getType() {
        return CardType.MULTIPLIER;
    }
}

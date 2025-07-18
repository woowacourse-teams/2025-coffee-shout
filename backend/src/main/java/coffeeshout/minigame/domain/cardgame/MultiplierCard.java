package coffeeshout.minigame.domain.cardgame;

public record MultiplierCard(
        Integer value
) implements Card {

    @Override
    public CardType getType() {
        return CardType.MULTIPLIER;
    }
}

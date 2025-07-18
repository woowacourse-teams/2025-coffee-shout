package coffeeshout.minigame.domain.cardgame;

public record AdditionCard(
        Integer value
) implements Card {

    @Override
    public CardType getType() {
        return CardType.ADDITION;
    }
}

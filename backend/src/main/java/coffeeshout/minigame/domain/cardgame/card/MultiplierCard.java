package coffeeshout.minigame.domain.cardgame.card;

public class MultiplierCard extends Card {

    public static final MultiplierCard QUADRUPLE = new MultiplierCard(4);
    public static final MultiplierCard DOUBLE = new MultiplierCard(2);
    public static final MultiplierCard NULLIFY = new MultiplierCard(0);
    public static final MultiplierCard INVERT = new MultiplierCard(-1);

    public MultiplierCard(int value) {
        super(CardType.MULTIPLIER, value);
    }
}

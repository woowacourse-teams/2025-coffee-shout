package coffeeshout.minigame.domain.cardgame.card;

public class MultiplierCard extends Card {

    public final static MultiplierCard QUADRUPLE = new MultiplierCard(4);
    public final static MultiplierCard DOUBLE = new MultiplierCard(2);
    public final static MultiplierCard NULLIFY = new MultiplierCard(0);
    public final static MultiplierCard INVERT = new MultiplierCard(-1);

    public MultiplierCard(int value) {
        super(CardType.MULTIPLIER, value);
    }
}

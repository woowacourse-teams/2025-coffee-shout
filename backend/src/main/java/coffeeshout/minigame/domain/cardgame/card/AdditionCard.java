package coffeeshout.minigame.domain.cardgame.card;

public class AdditionCard extends Card {

    public final static AdditionCard PLUS_40 = new AdditionCard(40);
    public final static AdditionCard PLUS_30 = new AdditionCard(30);
    public final static AdditionCard PLUS_20 = new AdditionCard(20);
    public final static AdditionCard PLUS_10 = new AdditionCard(10);
    public final static AdditionCard ZERO = new AdditionCard(0);
    public final static AdditionCard MINUS_10 = new AdditionCard(-10);
    public final static AdditionCard MINUS_20 = new AdditionCard(-20);
    public final static AdditionCard MINUS_30 = new AdditionCard(-30);
    public final static AdditionCard MINUS_40 = new AdditionCard(-40);

    public AdditionCard(int value) {
        super(CardType.ADDITION, value);
    }
}

package coffeeshout.minigame.domain.cardgame.card;

public class AdditionCard extends Card {

    public static final AdditionCard PLUS_40 = new AdditionCard(40);
    public static final AdditionCard PLUS_30 = new AdditionCard(30);
    public static final AdditionCard PLUS_20 = new AdditionCard(20);
    public static final AdditionCard PLUS_10 = new AdditionCard(10);
    public static final AdditionCard ZERO = new AdditionCard(0);
    public static final AdditionCard MINUS_10 = new AdditionCard(-10);
    public static final AdditionCard MINUS_20 = new AdditionCard(-20);
    public static final AdditionCard MINUS_30 = new AdditionCard(-30);
    public static final AdditionCard MINUS_40 = new AdditionCard(-40);

    public AdditionCard(int value) {
        super(CardType.ADDITION, value);
    }
}

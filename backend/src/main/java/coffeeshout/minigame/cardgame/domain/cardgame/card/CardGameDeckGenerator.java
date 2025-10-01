package coffeeshout.minigame.cardgame.domain.cardgame.card;

public interface CardGameDeckGenerator {

    Deck generate(int additionCardCount, int multiplierCardCount, long seed);
}

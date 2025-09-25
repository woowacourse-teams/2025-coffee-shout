package coffeeshout.minigame.domain.cardgame.card;

public interface CardGameDeckGenerator {

    Deck generate(int additionCardCount, int multiplierCardCount, long seed);
}

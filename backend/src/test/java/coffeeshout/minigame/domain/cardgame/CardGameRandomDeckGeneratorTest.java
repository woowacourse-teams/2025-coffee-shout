package coffeeshout.minigame.domain.cardgame;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.CardType;
import coffeeshout.minigame.domain.cardgame.card.Deck;
import org.junit.jupiter.api.Test;

class CardGameRandomDeckGeneratorTest {

    @Test
    void 카드를_랜덤으로_9장_뽑는다() {
        // Given
        int additionCardCount = 6;
        int multiplierCardCount = 3;

        final CardGameDeckGenerator cardGameDeck = new CardGameRandomDeckGenerator();
        Deck deck = cardGameDeck.generate(additionCardCount, multiplierCardCount);

        // When & Then
        assertThat(deck.size()).isEqualTo(9);

        long generalCardCount = deck.stream()
                .filter(card -> card.getType() == CardType.ADDITION)
                .count();
        assertThat(generalCardCount).isEqualTo(6);

        long specialCardCount = deck.stream()
                .filter(card -> card.getType() == CardType.MULTIPLIER)
                .count();
        assertThat(specialCardCount).isEqualTo(3);
    }

}

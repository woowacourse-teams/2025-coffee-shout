package coffeeshout.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CardGameDeckTest {

    @Test
    void 카드를_랜덤으로_9장_뽑는다() {
        // Given
        List<Card> cards = CardGameDeck.spreadCards();

        // When & Then
        assertThat(cards).hasSize(9);

        long generalCardCount = cards.stream()
                .filter(card -> card instanceof GeneralCard)
                .count();
        assertThat(generalCardCount).isEqualTo(6);

        long specialCardCount = cards.stream()
                .filter(card -> card instanceof SpecialCard)
                .count();
        assertThat(specialCardCount).isEqualTo(3);
    }

}

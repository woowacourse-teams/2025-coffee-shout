package coffeeshout.minigame.domain.cardgame.card;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MultiplierCardsTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void 요청한_개수만큼_카드를_뽑는다(int count) {
        // given
        MultiplierCards multiplierCards = new MultiplierCards();

        // when
        List<Card> pickedCards = multiplierCards.pickCards(count);

        // then
        assertThat(pickedCards).hasSize(count);
        assertThat(pickedCards).allMatch(card -> card instanceof MultiplierCard);
    }

    @Test
    void 최대_사용_가능한_카드_수를_초과하면_예외가_발생한다() {
        // given
        MultiplierCards multiplierCards = new MultiplierCards();
        int count = 5;

        // when & then
        assertThatThrownBy(() -> multiplierCards.pickCards(count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최대 사용 가능한 카드 수를 초과했습니다.");

    }
}

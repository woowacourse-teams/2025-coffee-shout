package coffeeshout.minigame.domain.cardgame.card;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GameCardTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void 요청한_개수만큼_곱셈_카드를_뽑는다(int count) {
        // given
        List<Card> multiplierCards = GameCard.getRandomMultiplyCards(count);

        // then
        AssertionsForInterfaceTypes.assertThat(multiplierCards).hasSize(count);
        AssertionsForInterfaceTypes.assertThat(multiplierCards).allMatch(card -> card.getType() == CardType.MULTIPLIER);
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 6})
    void 요청한_개수만큼_덧셈_카드를_뽑는다(int count) {
        // given
        List<Card> additionCards = GameCard.getRandomAdditionCards(count);

        // then
        AssertionsForInterfaceTypes.assertThat(additionCards).hasSize(count);
        AssertionsForInterfaceTypes.assertThat(additionCards).allMatch(card -> card.getType() == CardType.ADDITION);
    }

    @Test
    void 최대_사용_가능한_곱셈_카드_수를_초과하면_예외가_발생한다() {
        // given
        int count = 5;

        // when & then
        assertThatThrownBy(() -> GameCard.getRandomMultiplyCards(count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최대 사용 가능한 카드 수를 초과했습니다.");
    }

    @Test
    void 최대_사용_가능한_덧셈_카드_수를_초과하면_예외가_발생한다() {
        // given
        int count = 20;

        // when & then
        assertThatThrownBy(() -> GameCard.getRandomAdditionCards(count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최대 사용 가능한 카드 수를 초과했습니다.");
    }
}

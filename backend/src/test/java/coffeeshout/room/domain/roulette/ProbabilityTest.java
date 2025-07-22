package coffeeshout.room.domain.roulette;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.minigame.domain.MiniGameResultType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ProbabilityTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, 10001, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void 확률이_범위를_벗어나면_예외가_발생한다(int invalidValue) {
        assertThatThrownBy(() -> new Probability(invalidValue))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 5})
    void 확률을_나눈다(int divisor) {
        // given
        Probability probability = new Probability(10000);

        // when
        Probability result = probability.divide(divisor);

        // then
        assertThat(result.value()).isEqualTo(10000 / divisor);
    }

    @Test
    void 확률을_곱한다() {
        // given
        Probability probability = new Probability(1000);

        // when
        Probability result = probability.multiple(3);

        // then
        assertThat(result.value()).isEqualTo(3000);
    }

    @Test
    void 확률을_더한다() {
        // given
        Probability probability1 = new Probability(3000);
        Probability probability2 = new Probability(2000);

        // when
        Probability result = probability1.plus(probability2);

        // then
        assertThat(result.value()).isEqualTo(5000);
    }

    @Test
    void 확률을_뺀다() {
        // given
        Probability probability1 = new Probability(7000);
        Probability probability2 = new Probability(2500);

        // when
        Probability result = probability1.minus(probability2);

        // then
        assertThat(result.value()).isEqualTo(4500);
    }

    @ParameterizedTest(name = "{0}일 때 {1}을 조정하면 결과는 {2}")
    @CsvSource({
            "WINNER, 1000, 4000",
            "LOSER, 1000, 6000",
            "UNDECIDED, 1000, 5000"
    })
    void 결과유형에_따라_확률을_조정한다(MiniGameResultType type, int adjust, int expected) {
        // given
        Probability base = new Probability(5000);
        Probability adjustment = new Probability(adjust);

        // when
        Probability result = base.adjust(type, adjustment);

        // then
        assertThat(result.value()).isEqualTo(expected);
    }
}

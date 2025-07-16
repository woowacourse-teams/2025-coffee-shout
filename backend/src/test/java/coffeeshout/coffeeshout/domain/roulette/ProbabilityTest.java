package coffeeshout.coffeeshout.domain.roulette;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProbabilityTest {

    @ParameterizedTest
    @ValueSource(ints = { -10001, 10001, Integer.MIN_VALUE, Integer.MAX_VALUE })
    void 확률이_범위를_벗어나면_예외가_발생한다(int invalidValue) {
        assertThatThrownBy(() -> new Probability(invalidValue))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 4, 5 })
    void 확률을_나눈다(int divisor) {
        // given
        final Probability probability = new Probability(10000);

        // when
        final Probability result = probability.divide(divisor);

        // then
        assertThat(result.value()).isEqualTo(10000 / divisor);
    }

    @Test
    void 확률을_더한다() {
        // given
        final Probability probability1 = new Probability(3000);
        final Probability probability2 = new Probability(2000);

        // when
        final Probability result = probability1.plus(probability2);

        // then
        assertThat(result.value()).isEqualTo(5000);
    }

    @Test
    void 확률을_빼면_양수가_된다() {
        // given
        final Probability probability1 = new Probability(7000);
        final Probability probability2 = new Probability(2500);

        // when
        final Probability result = probability1.minus(probability2);

        // then
        assertThat(result.value()).isEqualTo(4500);
    }

    @Test
    void 확률을_빼면_음수가_될_수_있다() {
        // given
        final Probability probability1 = new Probability(2000);
        final Probability probability2 = new Probability(6000);

        // when
        final Probability result = probability1.minus(probability2);

        // then
        assertThat(result.value()).isEqualTo(-4000);
    }

    @Test
    void 확률의_부호를_반전한다() {
        final Probability positive = new Probability(3500);
        final Probability negative = new Probability(-4500);

        final Probability invertedPositive = positive.invert();
        final Probability invertedNegative = negative.invert();

        assertThat(invertedPositive.value()).isEqualTo(-3500);
        assertThat(invertedNegative.value()).isEqualTo(4500);
    }

    @Test
    void 확률_덧셈_결과가_범위를_초과하면_예외가_발생한다() {
        final Probability probability1 = new Probability(8000);
        final Probability probability2 = new Probability(3000);

        assertThatThrownBy(() -> probability1.plus(probability2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 확률_뺄셈_결과가_범위를_벗어나면_예외가_발생한다() {
        final Probability probability1 = new Probability(-9000);
        final Probability probability2 = new Probability(2000);

        assertThatThrownBy(() -> probability1.minus(probability2))
                .isInstanceOf(IllegalStateException.class);
    }
}

package coffeeshout.room.domain.probability;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AdjustmentProbabilityTest {

    @Test
    void 조정_확률을_더한다() {
        // given
        AdjustmentProbability adjustment1 = new AdjustmentProbability(3000);
        AdjustmentProbability adjustment2 = new AdjustmentProbability(2000);

        // when
        AdjustmentProbability result = adjustment1.plus(adjustment2);

        // then
        assertThat(result.value()).isEqualTo(5000);
    }

    @Test
    void 음수값과_더할_수_있다() {
        // given
        AdjustmentProbability positive = new AdjustmentProbability(5000);
        AdjustmentProbability negative = new AdjustmentProbability(-3000);

        // when
        AdjustmentProbability result = positive.plus(negative);

        // then
        assertThat(result.value()).isEqualTo(2000);
    }

    @Test
    void 결과값이_음수가_될_수_있다() {
        // given
        AdjustmentProbability negative = new AdjustmentProbability(-6000);
        AdjustmentProbability positive = new AdjustmentProbability(1000);

        // when
        AdjustmentProbability result = negative.plus(positive);

        // then
        assertThat(result.value()).isEqualTo(-5000);
    }
}

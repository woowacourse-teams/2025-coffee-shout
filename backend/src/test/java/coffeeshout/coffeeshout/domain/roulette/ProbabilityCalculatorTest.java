package coffeeshout.coffeeshout.domain.roulette;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class ProbabilityCalculatorTest {

    @Test
    void 방에_참여한_인원수에_따라_각_플레이어의_초기_확률을_계산한다() {
        // when
        Probability probability = ProbabilityCalculator.computeInitialProbability(4);

        // then
        assertThat(probability).isEqualTo(new Probability(2500));
    }

    @Test
    void 등수별_가감_확률을_계산한다() {
        // given
        ProbabilityCalculator probabilityCalculator = new ProbabilityCalculator(4, 5);

        // when
        Probability probability1 = probabilityCalculator.getAdjustProbability(1);
        Probability probability2 = probabilityCalculator.getAdjustProbability(2);
        Probability probability3 = probabilityCalculator.getAdjustProbability(3);
        Probability probability4 = probabilityCalculator.getAdjustProbability(4);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(probability1).isEqualTo(new Probability(-500));
            softly.assertThat(probability2).isEqualTo(new Probability(-250));
            softly.assertThat(probability3).isEqualTo(new Probability(250));
            softly.assertThat(probability4).isEqualTo(new Probability(500));
        });
    }

}

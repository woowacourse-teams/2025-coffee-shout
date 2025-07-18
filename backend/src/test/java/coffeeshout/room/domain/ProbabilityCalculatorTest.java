package coffeeshout.room.domain;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class ProbabilityCalculatorTest {

    @Test
    void 방에_참여한_인원수에_따라_각_플레이어의_초기_확률을_계산한다() {
        // given
        // when
        Probability probability = ProbabilityCalculator.computeInitialProbability(4);

        // then
        assertThat(probability).isEqualTo(new Probability(2500));
    }

    @Test
    void 등수별_가감_확률을_계산한다() {
        // given
        int playerCount = 4;
        int roundCount = 3;
        ProbabilityCalculator probabilityCalculator = new ProbabilityCalculator(playerCount, roundCount);

        // when
        Probability probability1 = probabilityCalculator.calculateAdjustProbability(playerCount, 1);
        Probability probability2 = probabilityCalculator.calculateAdjustProbability(playerCount, 2);
        Probability probability3 = probabilityCalculator.calculateAdjustProbability(playerCount, 3);
        Probability probability4 = probabilityCalculator.calculateAdjustProbability(playerCount, 4);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(probability1).isEqualTo(new Probability(832));
            softly.assertThat(probability2).isEqualTo(new Probability(416));
            softly.assertThat(probability3).isEqualTo(new Probability(416));
            softly.assertThat(probability4).isEqualTo(new Probability(832));
        });
    }
}

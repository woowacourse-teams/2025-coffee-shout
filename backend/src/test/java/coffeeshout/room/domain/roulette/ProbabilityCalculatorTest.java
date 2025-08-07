package coffeeshout.room.domain.roulette;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProbabilityCalculatorTest {

    @Test
    void 등수별_가감_확률을_계산한다() {
        // given
        int playerCount = 4;
        int roundCount = 3;
        ProbabilityCalculator probabilityCalculator = new ProbabilityCalculator(playerCount, roundCount);

        // when
        Probability probability1 = probabilityCalculator.calculateAdjustProbability(1);
        Probability probability2 = probabilityCalculator.calculateAdjustProbability(2);
        Probability probability3 = probabilityCalculator.calculateAdjustProbability(3);
        Probability probability4 = probabilityCalculator.calculateAdjustProbability(4);

        // then
        assertThat(probability1).isEqualTo(new Probability(832).multiple(0.7));
        assertThat(probability2).isEqualTo(new Probability(416).multiple(0.7));
        assertThat(probability3).isEqualTo(new Probability(416).multiple(0.7));
        assertThat(probability4).isEqualTo(new Probability(832).multiple(0.7));
    }
}

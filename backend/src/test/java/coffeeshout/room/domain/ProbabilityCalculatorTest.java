package coffeeshout.room.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import coffeeshout.room.domain.probability.AdjustmentProbability;
import coffeeshout.room.domain.probability.Probability;
import coffeeshout.room.domain.probability.ProbabilityCalculator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProbabilityCalculatorTest {

    @Nested
    class 생성자_검증 {

        @Test
        void 플레이어가_2명_미만이면_예외() {
            assertThatThrownBy(() -> new ProbabilityCalculator(1, 3))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("플레이어는 2명 이상이어야 합니다.");
        }

        @Test
        void 정상_생성() {
            assertDoesNotThrow(() -> new ProbabilityCalculator(4, 2));
        }
    }

    @Nested
    class 초기_확률_검증 {

        @Test
        void 플레이어가_4명이면_확률은_25_퍼센트() {
            Probability initial = ProbabilityCalculator.computeInitialProbability(4);
            assertThat(initial.value()).isEqualTo(2500);
        }

        @Test
        void 플레이어가_5명이면_확률은_20_퍼센트() {
            Probability initial = ProbabilityCalculator.computeInitialProbability(5);
            assertThat(initial.value()).isEqualTo(2000);
        }
    }

    @Nested
    class 등수별_확률_조정_검증 {

        @Test
        void 상위권은_양수값을_가진다() {
            ProbabilityCalculator calculator = new ProbabilityCalculator(4, 2);

            AdjustmentProbability result = calculator.computeAdjustProbability(1);

            assertThat(result.value()).isPositive();
        }

        @Test
        void 중간_등수는_0이다() {
            ProbabilityCalculator calculator = new ProbabilityCalculator(3, 3); // 홀수 → UNDECIDED

            AdjustmentProbability result = calculator.computeAdjustProbability(2); // 가운데

            assertThat(result.value()).isZero();
        }

        @Test
        void 하위권은_음수값을_가진다() {
            ProbabilityCalculator calculator = new ProbabilityCalculator(4, 2);

            AdjustmentProbability result = calculator.computeAdjustProbability(4);

            assertThat(result.value()).isNegative();
        }
    }

    @Nested
    class 등수별_조정_확률_정확도_검증 {

        @Test
        void 플레이어_4명_2라운드일_때_각_등수별_조정값() {
            // given
            ProbabilityCalculator calculator = new ProbabilityCalculator(4, 2);
            // initial = 10000 / 4 = 2500
            // maxAdjust = 2500 / 2 = 1250
            // step = 1250 / 2 = 625

            // when
            AdjustmentProbability rank1 = calculator.computeAdjustProbability(1); // WINNER, relRank = 1
            AdjustmentProbability rank2 = calculator.computeAdjustProbability(2); // WINNER, relRank = 2
            AdjustmentProbability rank3 = calculator.computeAdjustProbability(3); // LOSER, relRank = 2
            AdjustmentProbability rank4 = calculator.computeAdjustProbability(4); // LOSER, relRank = 1

            // then
            assertThat(rank1.value()).isEqualTo(625 * 2);     // +1250
            assertThat(rank2.value()).isEqualTo(625);         // +625
            assertThat(rank3.value()).isEqualTo(-625);        // -625
            assertThat(rank4.value()).isEqualTo(-625 * 2);    // -1250
        }

        @Test
        void 플레이어_5명_3라운드일_때_각_등수별_조정값() {
            // given
            ProbabilityCalculator calculator = new ProbabilityCalculator(5, 3);
            // initial = 10000 / 5 = 2000
            // maxAdjust = 2000 / 3 ≒ 666
            // step = 666 / 2 ≒ 333

            // when
            AdjustmentProbability rank1 = calculator.computeAdjustProbability(1); // WINNER, relRank=1
            AdjustmentProbability rank2 = calculator.computeAdjustProbability(2); // WINNER, relRank=2
            AdjustmentProbability rank3 = calculator.computeAdjustProbability(3); // UNDECIDED
            AdjustmentProbability rank4 = calculator.computeAdjustProbability(4); // LOSER, relRank=2
            AdjustmentProbability rank5 = calculator.computeAdjustProbability(5); // LOSER, relRank=1

            // then
            assertThat(rank1.value()).isEqualTo(333 * 2);      // +666
            assertThat(rank2.value()).isEqualTo(333);          // +333
            assertThat(rank3.value()).isEqualTo(0);            // 0
            assertThat(rank4.value()).isEqualTo(-333);         // -333
            assertThat(rank5.value()).isEqualTo(-333 * 2);     // -666
        }
    }
}

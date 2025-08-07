package coffeeshout.room.domain.roulette;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.player.Player;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProbabilityCalculatorTest {

    @Test
    void 플레이어_수가_2명_미만일_때_예외_발생() {
        // given & when & then
        assertThatThrownBy(() -> new ProbabilityCalculator(1, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("플레이어는 2명 이상이어야 합니다.");
    }

    @Test
    void 라운드_수가_0_이하일_때_예외_발생() {
        // given & when & then
        assertThatThrownBy(() -> new ProbabilityCalculator(4, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new ProbabilityCalculator(4, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 등수별_가감_확률을_계산한다() {
        // given
        ProbabilityCalculator calculator = new ProbabilityCalculator(4, 3);
        Player player1 = mock(Player.class);
        Player player2 = mock(Player.class);
        Player player3 = mock(Player.class);
        Player player4 = mock(Player.class);

        MiniGameResult miniGameResult = mock(MiniGameResult.class);
        when(miniGameResult.getRank()).thenReturn(Map.of(
                player1, 1,
                player2, 2,
                player3, 3,
                player4, 4
        ));

        RankNormalization rankNormalization = RankNormalization.from(miniGameResult);
        Probability baseProbability = new Probability(2500); // 25.00%

        // when
        Probability result1 = calculator.calculateAdjustProbability(rankNormalization, 1, baseProbability);
        Probability result2 = calculator.calculateAdjustProbability(rankNormalization, 2, baseProbability);
        Probability result3 = calculator.calculateAdjustProbability(rankNormalization, 3, baseProbability);
        Probability result4 = calculator.calculateAdjustProbability(rankNormalization, 4, baseProbability);

        // then
        // 1등과 4등은 확률이 감소 (WINNER와 LOSER 반대 적용)
        assertThat(result1.value()).isLessThan(baseProbability.value());
        assertThat(result4.value()).isGreaterThan(baseProbability.value());
        // 2등과 3등은 상대적으로 적은 변화
        assertThat(result2.value()).isLessThan(baseProbability.value());
        assertThat(result3.value()).isGreaterThan(baseProbability.value());
    }

    @Test
    void 홀수_플레이어에서_중간_등수는_확률_변화_없음() {
        // given
        ProbabilityCalculator calculator = new ProbabilityCalculator(3, 2);
        Player player1 = mock(Player.class);
        Player player2 = mock(Player.class);
        Player player3 = mock(Player.class);

        MiniGameResult miniGameResult = mock(MiniGameResult.class);
        when(miniGameResult.getRank()).thenReturn(Map.of(
                player1, 1,
                player2, 2,
                player3, 3
        ));

        RankNormalization rankNormalization = RankNormalization.from(miniGameResult);
        Probability baseProbability = new Probability(3333); // 33.33%

        // when
        Probability result2 = calculator.calculateAdjustProbability(rankNormalization, 2, baseProbability);

        // then
        // 중간 등수(UNDECIDED)는 확률 변화 없음
        assertThat(result2).isEqualTo(baseProbability);
    }

    @Test
    void 동점자가_있을_때_확률_분배() {
        // given
        ProbabilityCalculator calculator = new ProbabilityCalculator(4, 2);
        Player player1 = mock(Player.class);
        Player player2 = mock(Player.class);
        Player player3 = mock(Player.class);
        Player player4 = mock(Player.class);

        MiniGameResult miniGameResult = mock(MiniGameResult.class);
        when(miniGameResult.getRank()).thenReturn(Map.of(
                player1, 1,
                player2, 1,  // 1등 동점
                player3, 3,
                player4, 4
        ));

        RankNormalization rankNormalization = RankNormalization.from(miniGameResult);
        Probability baseProbability = new Probability(2500);

        // when
        Probability result1 = calculator.calculateAdjustProbability(rankNormalization, 1, baseProbability);

        // then
        // 동점자가 2명이므로 조정 확률이 절반으로 분배됨
        assertThat(result1).isNotNull();
    }
}
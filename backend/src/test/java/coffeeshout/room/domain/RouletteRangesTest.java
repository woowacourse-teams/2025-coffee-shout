package coffeeshout.room.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.player.domain.Player;
import org.junit.jupiter.api.Test;

class RouletteRangesTest {

    @Test
    void 숫자에_해당하는_플레이어를_찾는다() {
        // given
        Player player1 = PlayerFixture.한스();
        Player player2 = PlayerFixture.꾹이();
        RouletteProbabilities rouletteProbabilities = new RouletteProbabilities();

        rouletteProbabilities.add(player1);
        rouletteProbabilities.add(player2);

        RouletteRanges rouletteRanges = new RouletteRanges(rouletteProbabilities);

        // when
        Player result1 = rouletteRanges.pickPlayer(1500);
        Player result2 = rouletteRanges.pickPlayer(9000);

        // then
        assertThat(result1).isEqualTo(player1);
        assertThat(result2).isEqualTo(player2);
    }

    @Test
    void 범위를_벗어난_숫자를_입력하면_예외가_발생한다() {
        // given
        Player player = PlayerFixture.엠제이();
        RouletteProbabilities rouletteProbabilities = new RouletteProbabilities();
        rouletteProbabilities.add(player);

        RouletteRanges rouletteRanges = new RouletteRanges(rouletteProbabilities);

        // when & then
        assertThatThrownBy(() -> rouletteRanges.pickPlayer(10002))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 값이_없을_경우_endValue는_0을_반환한다() {
        // given
        RouletteProbabilities rouletteProbabilities = new RouletteProbabilities();

        RouletteRanges rouletteRanges = new RouletteRanges(rouletteProbabilities);

        // when
        int endValue = rouletteRanges.endValue();

        // then
        assertThat(endValue).isEqualTo(0);
    }

    @Test
    void 마지막_범위의_end값을_반환한다() {
        // given
        Player player1 = PlayerFixture.루키();
        Player player2 = PlayerFixture.엠제이();
        RouletteProbabilities rouletteProbabilities = new RouletteProbabilities();

        rouletteProbabilities.add(player1);
        rouletteProbabilities.add(player2);

        RouletteRanges rouletteRanges = new RouletteRanges(rouletteProbabilities);

        // when
        int result = rouletteRanges.endValue();

        // then
        assertThat(result).isEqualTo(10002);
    }
}

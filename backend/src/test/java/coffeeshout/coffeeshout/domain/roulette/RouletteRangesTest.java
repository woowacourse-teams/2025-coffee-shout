package coffeeshout.coffeeshout.domain.roulette;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.coffeeshout.domain.fixture.PlayerFixture;
import coffeeshout.coffeeshout.domain.player.Player;
import coffeeshout.coffeeshout.domain.player.PlayersWithProbability;
import org.junit.jupiter.api.Test;

class RouletteRangesTest {

    @Test
    void 숫자에_해당하는_플레이어를_찾는다() {
        // given
        final Player player1 = PlayerFixture.한스();
        final Player player2 = PlayerFixture.꾹이();
        final PlayersWithProbability playersWithProbability = new PlayersWithProbability();

        playersWithProbability.join(player1);
        playersWithProbability.join(player2);

        final RouletteRanges rouletteRanges = new RouletteRanges(playersWithProbability);

        // when
        final Player result1 = rouletteRanges.pickPlayer(1500);
        final Player result2 = rouletteRanges.pickPlayer(9000);

        // then
        assertThat(result1).isEqualTo(player1);
        assertThat(result2).isEqualTo(player2);
    }

    @Test
    void 범위를_벗어난_숫자를_입력하면_예외가_발생한다() {
        final Player player = PlayerFixture.엠제이();
        final PlayersWithProbability playersWithProbability = new PlayersWithProbability();
        playersWithProbability.join(player);

        final RouletteRanges rouletteRanges = new RouletteRanges(playersWithProbability);

        assertThatThrownBy(() -> rouletteRanges.pickPlayer(10002))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 값이_없을_경우_endValue는_0을_반환한다() {
        final PlayersWithProbability playersWithProbability = new PlayersWithProbability();

        final RouletteRanges rouletteRanges = new RouletteRanges(playersWithProbability);

        final int endValue = rouletteRanges.endValue();

        assertThat(endValue).isEqualTo(0);
    }

    @Test
    void 마지막_범위의_end값을_반환한다() {
        // given
        final Player player1 = PlayerFixture.루키();
        final Player player2 = PlayerFixture.엠제이();
        final PlayersWithProbability playersWithProbability = new PlayersWithProbability();

        playersWithProbability.join(player1);
        playersWithProbability.join(player2);

        final RouletteRanges rouletteRanges = new RouletteRanges(playersWithProbability);

        // when
        final int result = rouletteRanges.endValue();

        // then
        assertThat(result).isEqualTo(10002);
    }
}

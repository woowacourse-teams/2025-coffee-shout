package coffeeshout.coffeeshout.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.coffeeshout.domain.RouletteRanges;
import coffeeshout.coffeeshout.domain.fixture.PlayerFixture;
import coffeeshout.coffeeshout.domain.Player;
import coffeeshout.coffeeshout.domain.PlayersWithProbability;
import org.junit.jupiter.api.Test;

class RouletteRangesTest {

    @Test
    void 숫자에_해당하는_플레이어를_찾는다() {
        // given
        Player player1 = PlayerFixture.한스();
        Player player2 = PlayerFixture.꾹이();
        PlayersWithProbability playersWithProbability = new PlayersWithProbability();

        playersWithProbability.join(player1);
        playersWithProbability.join(player2);

        RouletteRanges rouletteRanges = new RouletteRanges(playersWithProbability);

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
        PlayersWithProbability playersWithProbability = new PlayersWithProbability();
        playersWithProbability.join(player);

        RouletteRanges rouletteRanges = new RouletteRanges(playersWithProbability);

        // when & then
        assertThatThrownBy(() -> rouletteRanges.pickPlayer(10002))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 값이_없을_경우_endValue는_0을_반환한다() {
        // given
        PlayersWithProbability playersWithProbability = new PlayersWithProbability();

        RouletteRanges rouletteRanges = new RouletteRanges(playersWithProbability);

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
        PlayersWithProbability playersWithProbability = new PlayersWithProbability();

        playersWithProbability.join(player1);
        playersWithProbability.join(player2);

        RouletteRanges rouletteRanges = new RouletteRanges(playersWithProbability);

        // when
        int result = rouletteRanges.endValue();

        // then
        assertThat(result).isEqualTo(10002);
    }
}

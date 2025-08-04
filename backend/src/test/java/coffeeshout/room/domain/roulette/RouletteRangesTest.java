package coffeeshout.room.domain.roulette;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.room.domain.player.Player;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RouletteRangesTest {

    @Test
    void 숫자에_해당하는_플레이어를_찾는다() {
        // given
        Player player1 = PlayerFixture.호스트한스();
        Player player2 = PlayerFixture.호스트꾹이();
        Map<Player, Probability> playerProbabilities = new LinkedHashMap<>();
        playerProbabilities.put(player1, new Probability(1500));
        playerProbabilities.put(player2, new Probability(8500));

        RouletteRanges rouletteRanges = new RouletteRanges(playerProbabilities);

        // when
        Player result1 = rouletteRanges.pickPlayer(1500);
        Player result2 = rouletteRanges.pickPlayer(10000);

        // then
        assertThat(result1).isEqualTo(player1);
        assertThat(result2).isEqualTo(player2);
    }

    @Test
    void 범위를_벗어난_숫자를_입력하면_예외가_발생한다() {
        // given
        Player player = PlayerFixture.호스트엠제이();

        Map<Player, Probability> playerProbabilities = new LinkedHashMap<>();
        playerProbabilities.put(player, new Probability(10000));

        RouletteRanges rouletteRanges = new RouletteRanges(playerProbabilities);

        // when & then
        assertThatThrownBy(() -> rouletteRanges.pickPlayer(10002))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 값이_없을_경우_endValue는_0을_반환한다() {
        // given
        Map<Player, Probability> playerProbabilities = new LinkedHashMap<>();
        RouletteRanges rouletteRanges = new RouletteRanges(playerProbabilities);

        // when
        int endValue = rouletteRanges.endValue();

        // then
        assertThat(endValue).isEqualTo(0);
    }

    @Test
    void 마지막_범위의_end값을_반환한다() {
        // given
        Player player1 = PlayerFixture.호스트한스();
        Player player2 = PlayerFixture.호스트꾹이();
        Map<Player, Probability> playerProbabilities = new LinkedHashMap<>();
        playerProbabilities.put(player1, new Probability(5000));
        playerProbabilities.put(player2, new Probability(5000));

        RouletteRanges rouletteRanges = new RouletteRanges(playerProbabilities);

        // when
        int result = rouletteRanges.endValue();

        // then
        assertThat(result).isEqualTo(10000);
    }
}

package coffeeshout.room.domain.roulette;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.fixture.MiniGameResultFixture;
import coffeeshout.fixture.PlayerFixture;
import coffeeshout.fixture.PlayerProbabilitiesFixture;
import coffeeshout.fixture.RouletteFixture;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.player.Player;
import org.junit.jupiter.api.Test;

class RouletteTest {

    @Test
    void 당첨자를_뽑는다() {
        // given
        Roulette roulette = RouletteFixture.고정_끝값_반환();

        // when
        Player result = roulette.spin();

        // then
        assertThat(result).isEqualTo(PlayerProbabilitiesFixture.PLAYERS.getLast());
    }

    @Test
    void 순위를_기반으로_확률을_조정한다() {
        // given
        MiniGameResult miniGameResult = MiniGameResultFixture.한스_루키_꾹이_엠제이();
        Roulette roulette = RouletteFixture.랜덤_반환();

        // when
        roulette.adjustProbabilities(miniGameResult, new ProbabilityCalculator(4, 5));

        // then
        assertThat(roulette.getProbability(PlayerFixture.호스트한스()))
                .isEqualTo(new Probability((int)(2500 - 500 * 0.7)));
        assertThat(roulette.getProbability(PlayerFixture.호스트루키()))
                .isEqualTo(new Probability((int)(2500 - 250 * 0.7)));
        assertThat(roulette.getProbability(PlayerFixture.호스트꾹이()))
                .isEqualTo(new Probability((int)(2500 + 250 * 0.7)));
        assertThat(roulette.getProbability(PlayerFixture.호스트엠제이()))
                .isEqualTo(new Probability((int)(2500 + 500 * 0.7)));

    }
}

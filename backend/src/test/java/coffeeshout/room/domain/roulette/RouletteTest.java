package coffeeshout.room.domain.roulette;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.fixture.MiniGameResultFixture;
import coffeeshout.fixture.PlayerFixture;
import coffeeshout.fixture.PlayerProbabilitiesFixture;
import coffeeshout.fixture.RouletteFixture;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.player.Player;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RouletteTest {

    private static final Logger log = LoggerFactory.getLogger(RouletteTest.class);

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
        SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(roulette.getProbability(PlayerFixture.호스트한스()))
                            .isEqualTo(new Probability((int) (2500 - 500 * 0.7)));
                    softly.assertThat(roulette.getProbability(PlayerFixture.게스트루키()))
                            .isEqualTo(new Probability((int) (2500 - 250 * 0.7)));
                    softly.assertThat(roulette.getProbability(PlayerFixture.게스트꾹이()))
                            .isEqualTo(new Probability((int) (2500 + 250 * 0.7)));
                    softly.assertThat(roulette.getProbability(PlayerFixture.게스트엠제이()))
                            .isEqualTo(new Probability((int) (2500 + 500 * 0.7)));
                }
        );
    }

    @Test
    void 플레이어_두_명에_대한_확률을_조정한다() {
        // given
        Player 한스 = PlayerFixture.호스트한스();
        Player 루키 = PlayerFixture.게스트루키();

        final Roulette roulette = new Roulette(new RoulettePicker());
        roulette.join(한스);
        roulette.join(루키);

        MiniGameResult miniGameResult = new MiniGameResult(Map.of(한스, 1, 루키, 2));

        // when
        roulette.adjustProbabilities(miniGameResult, new ProbabilityCalculator(2, 1));

        // then
        assertThat(roulette.getProbability(한스))
                .isEqualTo(new Probability((int) (5000 - 5000 * 0.7)));
        assertThat(roulette.getProbability(루키))
                .isEqualTo(new Probability((int) (5000 + 5000 * 0.7)));
    }

    @Test
    void 동점자에_대한_확률을_조정한다() {
        // given
        Player 한스 = PlayerFixture.호스트한스();
        Player 루키 = PlayerFixture.게스트루키();
        Player 꾹이 = PlayerFixture.게스트꾹이();

        final Roulette roulette = new Roulette(new RoulettePicker());
        roulette.join(한스);
        roulette.join(루키);
        roulette.join(꾹이);

        MiniGameResult miniGameResult = new MiniGameResult(Map.of(한스, 1, 루키, 2, 꾹이, 2));

        // when
        roulette.adjustProbabilities(miniGameResult, new ProbabilityCalculator(3, 1));

        // then

        // 루키와 꾹이는 동점이므로, 한스 - (5000 * 0.7) 을 가져가고,

        assertThat(roulette.getProbability(한스))
                .isEqualTo(new Probability((int) (3333 - (3333 * 0.7))));
        assertThat(roulette.getProbability(루키))
                .isEqualTo(new Probability((int) (3333 + (3333 * 0.7) / 2)));
        assertThat(roulette.getProbability(루키))
                .isEqualTo(new Probability((int) (3333 + (3333 * 0.7) / 2)));
    }
}

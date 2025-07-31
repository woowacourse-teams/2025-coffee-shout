package coffeeshout.room.domain.roulette;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.fixture.MiniGameResultFixture;
import coffeeshout.fixture.PlayerFixture;
import coffeeshout.fixture.PlayerProbabilities;
import coffeeshout.fixture.RouletteFixture;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.room.domain.player.Player;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RouletteTest {

    @Test
    void 당첨자를_뽑는다() {
        // given
        Roulette roulette = RouletteFixture.고정_끝값_반환();

        // when
        Player result = roulette.spin();

        // then
        assertThat(result).isEqualTo(PlayerProbabilities.PLAYERS.getLast());
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
                .isEqualTo(new Probability(2000));
        assertThat(roulette.getProbability(PlayerFixture.호스트루키()))
                .isEqualTo(new Probability(2250));
        assertThat(roulette.getProbability(PlayerFixture.호스트꾹이()))
                .isEqualTo(new Probability(2750));
        assertThat(roulette.getProbability(PlayerFixture.호스트엠제이()))
                .isEqualTo(new Probability(3000));
    }

    @Test
    void 새로운_플레이어_참여시_확률이_균등하게_재분배된다() {
        // given
        Roulette roulette = new Roulette(new RoulettePicker());
        Player player1 = PlayerFixture.호스트루키();
        Player player2 = PlayerFixture.호스트한스();

        // when & then
        roulette.join(player1);

        assertThat(roulette.getProbabilities())
                .containsOnlyKeys(player1);
        assertThat(roulette.getProbability(player1).value())
                .isEqualTo(10000);

        // when: 2명째 참여
        roulette.join(player2);

        // then
        int totalProbability = roulette.getProbabilities()
                .values()
                .stream()
                .mapToInt(Probability::value)
                .sum();

        assertThat(totalProbability)
                .isEqualTo(10000);

        // then: 균등 분배 검증
        assertThat(roulette.getProbabilities())
                .containsOnlyKeys(player1, player2);

        assertThat(roulette.getProbabilities().values())
                .allMatch(probability -> probability.value() == 5000);
    }
}

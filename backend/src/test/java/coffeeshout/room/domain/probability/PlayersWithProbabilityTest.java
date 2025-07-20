package coffeeshout.room.domain.probability;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class PlayersWithProbabilityTest {

    private final Player 한스 = PlayerFixture.한스();
    private final Player 루키 = PlayerFixture.루키();
    private final Player 꾹이 = PlayerFixture.꾹이();
    private final Player 엠제이 = PlayerFixture.엠제이();

    @Test
    void 네명의_플레이어가_두_번의_미니게임_후_확률이_올바르게_계산된다() {
        // given
        List<Player> players = List.of(한스, 루키, 꾹이, 엠제이);

        MiniGameResult result1 = new MiniGameResult(Map.of(
                한스, 1,
                루키, 2,
                꾹이, 3,
                엠제이, 4
        ));

        MiniGameResult result2 = new MiniGameResult(Map.of(
                루키, 1,
                한스, 2,
                엠제이, 3,
                꾹이, 4
        ));

        List<MiniGameResult> results = List.of(result1, result2);
        ProbabilityCalculator calculator = new ProbabilityCalculator(4, 2);

        // when
        PlayersWithProbability playersWithProbability =
                PlayersWithProbability.probability(players, results, calculator);

        // then
        int total = players.stream()
                .mapToInt(player -> playersWithProbability.getProbability(player).value())
                .sum();

        SoftAssertions.assertSoftly(softly -> {
            assertThat(total).isEqualTo(10000);
            assertThat(playersWithProbability.getProbability(한스)).isEqualTo(new Probability(4375));
            assertThat(playersWithProbability.getProbability(루키)).isEqualTo(new Probability(4375));
            assertThat(playersWithProbability.getProbability(꾹이)).isEqualTo(new Probability(625));
            assertThat(playersWithProbability.getProbability(엠제이)).isEqualTo(new Probability(625));
        });
    }
}

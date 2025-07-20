package coffeeshout.room.domain;

import coffeeshout.fixture.MiniGameResultFixture;
import coffeeshout.fixture.PlayerFixture;
import coffeeshout.fixture.PlayersFixture;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class PlayersWithProbabilityTest {

    @Test
    void 순위를_기반으로_확률을_조정한다() {
        // given
        RouletteProbabilities rouletteProbabilities = new RouletteProbabilities();

        for (Player player : PlayersFixture.playerList) {
            rouletteProbabilities.add(player);
        }

        MiniGameResult miniGameResult = MiniGameResultFixture.한스_루키_꾹이_엠제이();

        // when
        rouletteProbabilities.adjustProbabilities(miniGameResult, 5);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(rouletteProbabilities.getProbability(PlayerFixture.한스()))
                    .isEqualTo(new Probability(2000));
            softly.assertThat(rouletteProbabilities.getProbability(PlayerFixture.루키()))
                    .isEqualTo(new Probability(2250));
            softly.assertThat(rouletteProbabilities.getProbability(PlayerFixture.꾹이()))
                    .isEqualTo(new Probability(2750));
            softly.assertThat(rouletteProbabilities.getProbability(PlayerFixture.엠제이()))
                    .isEqualTo(new Probability(3000));
        });
    }
}



package coffeeshout.room.domain;

import coffeeshout.fixture.MiniGameResultFixture;
import coffeeshout.fixture.PlayerFixture;
import coffeeshout.fixture.PlayersFixture;
import coffeeshout.minigame.domain.MiniGameResult;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class PlayersWithProbabilityTest {

    @Test
    void 순위를_기반으로_확률을_조정한다() {
        // given
        PlayersWithProbability playersWithProbability = PlayersFixture.꾹이_루키_엠제이_한스();
        MiniGameResult miniGameResult = MiniGameResultFixture.한스_루키_꾹이_엠제이();

        // when
        playersWithProbability.adjustProbabilities(miniGameResult, new ProbabilityCalculator(4, 5));

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(playersWithProbability.getProbability(PlayerFixture.한스()))
                    .isEqualTo(new Probability(2000));
            softly.assertThat(playersWithProbability.getProbability(PlayerFixture.루키()))
                    .isEqualTo(new Probability(2250));
            softly.assertThat(playersWithProbability.getProbability(PlayerFixture.꾹이()))
                    .isEqualTo(new Probability(2750));
            softly.assertThat(playersWithProbability.getProbability(PlayerFixture.엠제이()))
                    .isEqualTo(new Probability(3000));
        });
    }
}



package coffeeshout.coffeeshout.domain;

import coffeeshout.coffeeshout.domain.MiniGameResult;
import coffeeshout.coffeeshout.domain.PlayersWithProbability;
import coffeeshout.coffeeshout.domain.fixture.MiniGameResultFixture;
import coffeeshout.coffeeshout.domain.fixture.PlayerFixture;
import coffeeshout.coffeeshout.domain.fixture.PlayersFixture;
import coffeeshout.coffeeshout.domain.Probability;
import coffeeshout.coffeeshout.domain.ProbabilityCalculator;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class PlayersWithProbabilityTest {

    @Test
    void 순위를_기반으로_확률을_조정한다(){
        // given
        PlayersWithProbability playersWithProbability = PlayersFixture.꾹이_루키_엠제이_한스();
        MiniGameResult miniGameResult = MiniGameResultFixture.한스_루키_꾹이_엠제이();

        // when
        playersWithProbability.adjustProbabilities(miniGameResult, new ProbabilityCalculator(4, 5));

        // then
        SoftAssertions.assertSoftly(softly ->{
            softly.assertThat(playersWithProbability.getProbability(PlayerFixture.한스())).isEqualTo(new Probability(2000));
            softly.assertThat(playersWithProbability.getProbability(PlayerFixture.루키())).isEqualTo(new Probability(2250));
            softly.assertThat(playersWithProbability.getProbability(PlayerFixture.꾹이())).isEqualTo(new Probability(2750));
            softly.assertThat(playersWithProbability.getProbability(PlayerFixture.엠제이())).isEqualTo(new Probability(3000));

        });
    }
}



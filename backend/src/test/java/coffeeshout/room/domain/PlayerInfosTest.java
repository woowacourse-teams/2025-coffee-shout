package coffeeshout.room.domain;

import coffeeshout.fixture.MiniGameResultFixture;
import coffeeshout.fixture.PlayerFixture;
import coffeeshout.fixture.PlayerInfosFixture;
import coffeeshout.minigame.domain.MiniGameResult;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class PlayerInfosTest {

    @Test
    void 순위를_기반으로_확률을_조정한다() {
        // given
        PlayerInfos playerInfos = PlayerInfosFixture.꾹이_루키_엠제이_한스();
        MiniGameResult miniGameResult = MiniGameResultFixture.한스_루키_꾹이_엠제이();

        // when
        playerInfos.adjustProbabilities(miniGameResult, new ProbabilityCalculator(4, 5));

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(playerInfos.getProbability(PlayerFixture.한스()))
                    .isEqualTo(new Probability(2000));
            softly.assertThat(playerInfos.getProbability(PlayerFixture.루키()))
                    .isEqualTo(new Probability(2250));
            softly.assertThat(playerInfos.getProbability(PlayerFixture.꾹이()))
                    .isEqualTo(new Probability(2750));
            softly.assertThat(playerInfos.getProbability(PlayerFixture.엠제이()))
                    .isEqualTo(new Probability(3000));
        });
    }
}



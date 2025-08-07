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
import org.junit.jupiter.api.Nested;
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

    @Nested
    class 동점자_테스트 {

        @Test
        void _3명_중_2등_동점자_2명() {
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
            // 한스 1등, 루키/꾹이 2등 동점 (2명이므로 확률 조정량을 2로 나눔)
            // 기본 확률: 3333
            // 1등 조정: -3333 * 0.7 = -2333
            // 2등 조정: +3333 * 0.7 / 2 = +1166 (동점자 2명이므로 나눔)

            assertThat(roulette.getProbability(한스))
                    .isEqualTo(new Probability((3333 - (int) (3333 * 0.7))));
            assertThat(roulette.getProbability(루키))
                    .isEqualTo(new Probability((3333 + (int) (3333 * 0.7) / 2)));
            assertThat(roulette.getProbability(꾹이))
                    .isEqualTo(new Probability((3333 + (int) (3333 * 0.7) / 2)));
        }

        @Test
        void _4명_중_2등_동점자_2명() {
            // given
            Player 한스 = PlayerFixture.호스트한스();
            Player 루키 = PlayerFixture.게스트루키();
            Player 꾹이 = PlayerFixture.게스트꾹이();
            Player 엠제이 = PlayerFixture.게스트엠제이();

            final Roulette roulette = new Roulette(new RoulettePicker());
            roulette.join(한스);
            roulette.join(루키);
            roulette.join(꾹이);
            roulette.join(엠제이);

            MiniGameResult miniGameResult = new MiniGameResult(Map.of(한스, 1, 루키, 2, 꾹이, 2, 엠제이, 4));

            // when
            roulette.adjustProbabilities(miniGameResult, new ProbabilityCalculator(4, 1));

            // then
            // 한스 1등, 루키/꾹이 2등 동점 (2명이므로 확률 조정량을 2로 나눔)
            // 기본 확률: 2500
            // 1등 조정: -2500 * 0.7 = -1750
            // 2등 조정: 조정 x
            // 4등 조정 +2500 * 0.7 / 2 = +1750 (동점자 2명이므로 나눔)

            SoftAssertions.assertSoftly(
                    softly -> {
                        softly.assertThat(roulette.getProbability(한스))
                                .isEqualTo(new Probability((2500 - (int) (2500 * 0.7))));
                        softly.assertThat(roulette.getProbability(루키))
                                .isEqualTo(new Probability(2500));
                        softly.assertThat(roulette.getProbability(꾹이))
                                .isEqualTo(new Probability(2500));
                        softly.assertThat(roulette.getProbability(엠제이))
                                .isEqualTo(new Probability((2500 + (int) (2500 * 0.7))));
                    }
            );
        }

        @Test
        void _4명_중_3등_동점자_2명() {
            // given
            Player 한스 = PlayerFixture.호스트한스();
            Player 루키 = PlayerFixture.게스트루키();
            Player 꾹이 = PlayerFixture.게스트꾹이();
            Player 엠제이 = PlayerFixture.게스트엠제이();

            final Roulette roulette = new Roulette(new RoulettePicker());
            roulette.join(한스);
            roulette.join(루키);
            roulette.join(꾹이);
            roulette.join(엠제이);

            MiniGameResult miniGameResult = new MiniGameResult(Map.of(한스, 1, 루키, 2, 꾹이, 3, 엠제이, 3));

            // when
            roulette.adjustProbabilities(miniGameResult, new ProbabilityCalculator(4, 1));

            // then
            // 한스 1등, 꾹이/엠제이 3등 동점 (2명이므로 확률 조정량을 2로 나눔)
            // 기본 확률: 2500
            // 1등 조정: -2500 * 0.7 = -1750
            // 2등 조정: 조정 x
            // 3등 조정 +2500 * 0.7 / 2 = +875(동점자 2명이므로 나눔)

            SoftAssertions.assertSoftly(
                    softly -> {
                        softly.assertThat(roulette.getProbability(한스))
                                .isEqualTo(new Probability((2500 - (int) (2500 * 0.7))));
                        softly.assertThat(roulette.getProbability(루키))
                                .isEqualTo(new Probability(2500));
                        softly.assertThat(roulette.getProbability(꾹이))
                                .isEqualTo(new Probability(2500 + (int) (2500 * 0.7) / 2));
                        softly.assertThat(roulette.getProbability(엠제이))
                                .isEqualTo(new Probability(2500 + (int) (2500 * 0.7) / 2));
                    }
            );
        }

        @Test
        void _4명_중_2등_동점자_3명() {
            // given
            Player 한스 = PlayerFixture.호스트한스();
            Player 루키 = PlayerFixture.게스트루키();
            Player 꾹이 = PlayerFixture.게스트꾹이();
            Player 엠제이 = PlayerFixture.게스트엠제이();

            final Roulette roulette = new Roulette(new RoulettePicker());
            roulette.join(한스);
            roulette.join(루키);
            roulette.join(꾹이);
            roulette.join(엠제이);

            MiniGameResult miniGameResult = new MiniGameResult(Map.of(한스, 1, 루키, 2, 꾹이, 2, 엠제이, 2));

            // when
            roulette.adjustProbabilities(miniGameResult, new ProbabilityCalculator(4, 1));

            // then
            // 기본 확률: 2500
            // 1등 조정: -2500 * 0.7 = -1750
            // 2등 조정: 조정 x
            // 4등 조정 +2500 * 0.7 / 3 = +583(동점자 3명이므로 나눔)

            SoftAssertions.assertSoftly(
                    softly -> {
                        softly.assertThat(roulette.getProbability(한스))
                                .isEqualTo(new Probability((2500 - (int) (2500 * 0.7))));
                        softly.assertThat(roulette.getProbability(루키))
                                .isEqualTo(new Probability(2500 + (int) (2500 * 0.7) / 3));
                        softly.assertThat(roulette.getProbability(꾹이))
                                .isEqualTo(new Probability(2500 + (int) (2500 * 0.7) / 3));
                        softly.assertThat(roulette.getProbability(엠제이))
                                .isEqualTo(new Probability(2500 + (int) (2500 * 0.7) / 3));
                    }
            );
        }
    }
}

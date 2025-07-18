package coffeeshout.minigame.domain;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.room.domain.Probability;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class MiniGameResultTypeTest {

    @ParameterizedTest
    @CsvSource({
            "2, 1",
            "3, 1",
            "4, 1",
            "4, 2"
    })
    void 순위가_상위일_경우_WINNER를_반환한다(int playerCount, int rank) {
        // given

        // when
        MiniGameResultType resultType = MiniGameResultType.of(playerCount, rank);

        // then
        assertThat(resultType).isEqualTo(MiniGameResultType.WINNER);
    }

    @ParameterizedTest
    @CsvSource({
            "3, 2",
            "5, 3",
            "7, 4"
    })
    void 플레이어수가_홀수이고_중간순위일_경우_UNDECIDED를_반환한다(int playerCount, int rank) {
        // given
        // when
        MiniGameResultType resultType = MiniGameResultType.of(playerCount, rank);

        // then
        assertThat(resultType).isEqualTo(MiniGameResultType.UNDECIDED);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 2",
            "3, 3",
            "4, 3",
            "4, 4"
    })
    void 순위가_하위일_경우_LOSER를_반환한다(int playerCount, int rank) {
        // given
        // when
        MiniGameResultType resultType = MiniGameResultType.of(playerCount, rank);

        // then
        assertThat(resultType).isEqualTo(MiniGameResultType.LOSER);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 1, 1000, 2000",
            "2, 2, 1000, 1000"
    })
    void WINNER는_상대순위에_비례해_양의_확률을_반환한다(int rankCount, int relativeRank, int stepValue, int expected) {
        // given
        MiniGameResultType type = MiniGameResultType.WINNER;
        Probability step = new Probability(stepValue);

        // when
        Probability result = type.adjustProbability(rankCount, relativeRank, step);

        // then
        assertThat(result.value()).isEqualTo(expected);
    }

    @Test
    void UNDECIDED는_항상_0의_확률을_반환한다() {
        // given
        MiniGameResultType type = MiniGameResultType.UNDECIDED;
        Probability step = new Probability(999);

        // when
        Probability result = type.adjustProbability(5, 3, step);

        // then
        assertThat(result.value()).isEqualTo(0);
    }
}

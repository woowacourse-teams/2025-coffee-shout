package coffeeshout.room.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.probability.PlayersWithProbability;
import coffeeshout.room.domain.probability.Probability;
import coffeeshout.room.domain.range.RouletteRanges;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RouletteRangesTest {

    private Player 한스 = PlayerFixture.한스();
    private Player 루키 = PlayerFixture.루키();
    private Player 꾹이 = PlayerFixture.꾹이();

    private PlayersWithProbability probabilities;

    @BeforeEach
    void setUp() {
        // 한스: 3000, 루키: 4000, 꾹이: 3000 → 총합 10000
        Map<Player, Probability> map = new LinkedHashMap<>();
        map.put(한스, new Probability(3000));
        map.put(루키, new Probability(4000));
        map.put(꾹이, new Probability(3000));
        probabilities = new PlayersWithProbability(map);
    }

    @Test
    void 룰렛_범위에_따라_플레이어가_선택된다() {
        // given
        RouletteRanges ranges = new RouletteRanges(probabilities);

        // when & then
        assertThat(ranges.pickPlayer(1000)).isEqualTo(한스);  // 1 ~ 3000
        assertThat(ranges.pickPlayer(3500)).isEqualTo(루키);  // 3001 ~ 7000
        assertThat(ranges.pickPlayer(9000)).isEqualTo(꾹이);  // 7001 ~ 10000
    }

    @Test
    void 범위에_없는_숫자는_예외가_발생한다() {
        // given
        RouletteRanges ranges = new RouletteRanges(probabilities);

        // when & then
        assertThatThrownBy(() -> ranges.pickPlayer(10001))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("범위에 해당하지 않는 숫자");
    }

    @Test
    void 마지막_범위의_끝값은_전체_확률합과_같다() {
        // given
        RouletteRanges ranges = new RouletteRanges(probabilities);

        // when
        int end = ranges.endValue();

        // then
        assertThat(end).isEqualTo(10000);
    }
}

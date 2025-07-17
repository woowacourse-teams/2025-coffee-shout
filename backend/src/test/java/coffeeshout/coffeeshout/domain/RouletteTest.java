package coffeeshout.coffeeshout.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.coffeeshout.domain.Roulette;
import coffeeshout.coffeeshout.domain.fixture.PlayersFixture;
import coffeeshout.coffeeshout.domain.fixture.RouletteFixture;
import coffeeshout.coffeeshout.domain.Player;
import coffeeshout.coffeeshout.domain.PlayersWithProbability;
import org.junit.jupiter.api.Test;

class RouletteTest {

    @Test
    void 당첨자를_뽑는다() {
        // given
        Roulette roulette = RouletteFixture.고정_끝값_반환();
        PlayersWithProbability playersWithProbability = PlayersFixture.꾹이_루키_엠제이_한스();

        // when
        Player result = roulette.spin(playersWithProbability);

        // then
        assertThat(result).isEqualTo(PlayersFixture.playerList.getLast());
    }
}

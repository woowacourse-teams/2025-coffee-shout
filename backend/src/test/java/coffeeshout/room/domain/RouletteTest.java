package coffeeshout.room.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.fixture.PlayersFixture;
import coffeeshout.fixture.RouletteFixture;
import coffeeshout.player.domain.Player;
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

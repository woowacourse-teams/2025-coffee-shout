package coffeeshout.room.domain;

import static org.assertj.core.api.Assertions.assertThat;

import coffeeshout.fixture.PlayersFixture;
import coffeeshout.fixture.RouletteFixture;
import coffeeshout.player.domain.Player;
import org.junit.jupiter.api.Test;

class RouletteTest {

    @Test
    void 당첨자를_뽑는다() {
        // given
        Roulette roulette = RouletteFixture.고정_끝값_반환();
        for (Player player : PlayersFixture.playerList) {
            roulette.join(player);
        }

        // when
        Player result = roulette.spin();

        // then
        assertThat(result).isEqualTo(PlayersFixture.playerList.getLast());
    }
}

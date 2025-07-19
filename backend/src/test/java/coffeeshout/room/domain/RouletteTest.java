package coffeeshout.room.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.fixture.PlayerInfosFixture;
import coffeeshout.fixture.RouletteFixture;
import coffeeshout.player.domain.Player;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RouletteTest {

    @Test
    void 당첨자를_뽑는다() {
        // given
        Roulette roulette = RouletteFixture.고정_끝값_반환();
        ReflectionTestUtils.setField(roulette, "playerInfos", PlayerInfosFixture.꾹이_루키_엠제이_한스());

        // when
        Player result = roulette.spin();

        // then
        assertThat(result).isEqualTo(PlayerInfosFixture.playerList.getLast());
    }
}

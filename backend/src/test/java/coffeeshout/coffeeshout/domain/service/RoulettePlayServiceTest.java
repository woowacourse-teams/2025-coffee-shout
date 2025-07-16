package coffeeshout.coffeeshout.domain.service;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.coffeeshout.domain.JoinCode;
import coffeeshout.coffeeshout.domain.Room;
import coffeeshout.coffeeshout.domain.RoomState;
import coffeeshout.coffeeshout.domain.fixture.PlayerFixture;
import coffeeshout.coffeeshout.domain.fixture.RouletteFixture;
import coffeeshout.coffeeshout.domain.player.Player;
import coffeeshout.coffeeshout.domain.roulette.Roulette;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RoulettePlayServiceTest {
    private final RoulettePlayService roulettePlayService = new RoulettePlayService();
    private final JoinCode joinCode = new JoinCode("ABCDEF");
    private final Roulette roulette = RouletteFixture.고정_끝값_반환();
    private final Player 호스트_한스 = PlayerFixture.한스();
    private final Player 게스트_꾹이 = PlayerFixture.꾹이();
    private final Room room = new Room(joinCode, roulette, 호스트_한스);;

    @Test
    void 룰렛_정상_시작_조건_충족() {
        // given
        room.joinPlayer(게스트_꾹이);
        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);

        // when
        Player loser = roulettePlayService.playRoulette(호스트_한스, room);

        // then
        assertThat(loser).isEqualTo(게스트_꾹이);
    }

    @Test
    void 호스트가_아니면_룰렛_시작_불가() {
        // given
        room.joinPlayer(게스트_꾹이);
        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);

        // when & then
        assertThatThrownBy(() -> roulettePlayService.playRoulette(게스트_꾹이, room))
                .isInstanceOf(IllegalStateException.class);
    }
}

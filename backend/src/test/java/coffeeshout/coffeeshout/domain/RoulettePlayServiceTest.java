package coffeeshout.coffeeshout.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.coffeeshout.domain.Room;
import coffeeshout.coffeeshout.domain.RoomState;
import coffeeshout.coffeeshout.domain.fixture.PlayerFixture;
import coffeeshout.coffeeshout.domain.fixture.RoomFixture;
import coffeeshout.coffeeshout.domain.Player;
import coffeeshout.coffeeshout.domain.RoulettePlayService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RoulettePlayServiceTest {
    private RoulettePlayService roulettePlayService = new RoulettePlayService();
    private Player 한스 = PlayerFixture.한스();
    private Player 꾹이 = PlayerFixture.꾹이();
    private Room room = RoomFixture.호스트_꾹이();

    @Test
    void 룰렛_정상_시작_조건_충족() {
        // given
        room.joinPlayer(한스);
        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);

        // when
        Player loser = roulettePlayService.playRoulette(꾹이, room);

        // then
        assertThat(loser).isEqualTo(한스);
    }

    @Test
    void 호스트가_아니면_룰렛_시작_불가() {
        // given
        room.joinPlayer(한스);
        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);

        // when & then
        assertThatThrownBy(() -> roulettePlayService.playRoulette(한스, room))
                .isInstanceOf(IllegalStateException.class);
    }
}

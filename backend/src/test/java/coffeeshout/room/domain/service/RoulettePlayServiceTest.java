package coffeeshout.room.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.fixture.RoomFixture;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.Player;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RoulettePlayServiceTest {

    private RoulettePlayService roulettePlayService = new RoulettePlayService();
    private Player 한스 = PlayerFixture.호스트한스();
    private Player 꾹이 = PlayerFixture.호스트꾹이();
    private Room room = RoomFixture.호스트_꾹이();

    @Test
    void 룰렛_정상_시작_조건_충족() {
        // given
        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);

        // when
        Player winner = roulettePlayService.playRoulette(꾹이, room);

        // then
        assertThat(winner).isEqualTo(한스);
    }

    @Test
    void 호스트가_아니면_룰렛_시작_불가() {
        // given
        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);

        // when & then
        assertThatThrownBy(() -> roulettePlayService.playRoulette(한스, room))
                .isInstanceOf(IllegalStateException.class);
    }
}

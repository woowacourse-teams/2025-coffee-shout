package coffeeshout.coffeeshout.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoomTest {

    @Test
    void 방을_생성한다() {
        // when
        Room room = new Room(
                new JoinCode("ABCSD"),
                new Roulette(),
                List.of(new MiniGame())
        );

        // then
        assertThat(room).isNotNull();
    }

    @Test
    void 방을_생성할때_미니게임이_없으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> new Room(
                new JoinCode("ABCSD"),
                new Roulette(),
                Collections.emptyList()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 방이_생성되었을때_준비_상태이다() {
        // given
        Room room = new Room(
                new JoinCode("ABCSD"),
                new Roulette(),
                List.of(new MiniGame())
        );

        // when & then
        assertThat(room.getRoomState()).isEqualTo(RoomState.READY);
    }

    @Test
    void 룰렛을_돌릴_때_플레이어가_두명_이상이_아닌_경우_예외가_발생한다() {
        // given
        Room room = new Room(
                new JoinCode("ABCSD"),
                new Roulette(),
                List.of(new MiniGame())
        );

        // when & then
        assertThatThrownBy(room::playRoulette).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 미니게임을_시작할_때_플레이어가_두명_이상이_아닌_경우_예외가_발생한다() {
        // given
        Room room = new Room(
                new JoinCode("ABCSD"),
                new Roulette(),
                List.of(new MiniGame())
        );

        // when & then
        assertThatThrownBy(room::playMiniGame).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 게임중이_아닐때_룰렛을_돌리면_예외가_발생한다() {
        // given
        Room room = new Room(
                new JoinCode("ABCSD"),
                new Roulette(),
                List.of(new MiniGame())
        );

        // when & then
        assertThatThrownBy(room::playRoulette).isInstanceOf(IllegalStateException.class);
    }


}

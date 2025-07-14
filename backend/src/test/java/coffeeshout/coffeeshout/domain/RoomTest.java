package coffeeshout.coffeeshout.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.coffeeshout.domain.player.Guest;
import coffeeshout.coffeeshout.domain.player.Host;
import coffeeshout.coffeeshout.domain.player.Player;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoomTest {

    private Menu menu = new Menu();
    private Room room = new Room(new JoinCode("ABC345"), new Roulette());
    private Host host = new Host(null, "hans", menu, room);

    @Test
    void 플레이어가_정상적으로_참가할_수_있다() {
        // when
        room.joinPlayer(host);

        // then
        assertThat(room.getPlayers()).containsExactly(host);
    }

    @Test
    void 중복된_이름으로_참가하면_예외가_발생한다() {
        // given
        room.joinPlayer(host);
        Player duplicate = new Guest(null, "hans", menu, room);

        // when & then
        assertThatThrownBy(() -> room.joinPlayer(duplicate))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 최대_인원을_초과하면_예외가_발생한다() {
        // given
        for (int i = 0; i < Room.MAXIMUM_GUEST_COUNT; i++) {
            room.joinPlayer(new Guest(null, "p" + i, menu, room));
        }

        // when & then
        assertThatThrownBy(() -> room.joinPlayer(new Guest(null, "overflow", menu, room)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void READY_상태가_아니면_참가할_수_없다() {
        // given
        room.joinPlayer(new Guest(null, "hans1", menu, room));
        room.joinPlayer(new Guest(null, "hans3", menu, room));
        room.setMiniGame(List.of(new MiniGame()));

        // when
        host.playMiniGame();

        // then
        assertThatThrownBy(() -> room.joinPlayer(new Guest(null, "late", menu, room)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 미니게임이_없으면_true를_반환한다() {
        // when & then
        assertThat(room.hasNoMiniGames()).isTrue();
    }

    @Test
    void 미니게임이_있으면_false를_반환한다() {
        // given
        room.setMiniGame(List.of(new MiniGame()));

        // when & then
        assertThat(room.hasNoMiniGames()).isFalse();
    }

    @Test
    void 플레이어가_충분하면_true를_반환한다() {
        // given
        room.joinPlayer(new Guest(null, "p1", menu, room));
        room.joinPlayer(new Guest(null, "p2", menu, room));

        // when & then
        assertThat(room.hasEnoughPlayers()).isTrue();
    }

    @Test
    void 플레이어가_충분하지_않으면_false를_반환한다() {
        // when & then
        assertThat(room.hasEnoughPlayers()).isFalse();
    }

    @Test
    void 미니게임은_5개_이하여야_한다_초과할_경우_예외_발생() {
        // when & then
        assertThatThrownBy(() -> room.setMiniGame(List.of(
                new MiniGame(),
                new MiniGame(),
                new MiniGame(),
                new MiniGame(),
                new MiniGame(),
                new MiniGame()
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 미니게임을_추가한다() {
        // when
        room.setMiniGame(List.of(new MiniGame()));

        // then
        assertThat(room.getMiniGames().size()).isEqualTo(1);
    }
}

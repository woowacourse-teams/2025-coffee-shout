package coffeeshout.coffeeshout.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.coffeeshout.domain.JoinCode;
import coffeeshout.coffeeshout.domain.Menu;
import coffeeshout.coffeeshout.domain.MiniGame;
import coffeeshout.coffeeshout.domain.Room;
import coffeeshout.coffeeshout.domain.roulette.JavaRandomGenerator;
import coffeeshout.coffeeshout.domain.roulette.Roulette;
import coffeeshout.coffeeshout.domain.player.Player;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MiniGamePlayServiceTest {

    private MiniGamePlayService service = new MiniGamePlayService();
    private List<Player> players = List.of(new Player(null, "꾹", null, null), new Player(null, "한스", null, null));
    private Room room = new Room(new JoinCode("AAAAAA"), new Roulette(players, 1, new JavaRandomGenerator()));
    private Player host = new Player(1L, "host", new Menu(), room);
    private Player guest = new Player(2L, "guest", new Menu(), room);

    @BeforeEach
    void setUp() {
        room.changeHost(host);
        room.joinPlayer(host);
    }

    @Test
    void 미니게임_조건_만족시_정상_시작() {
        // given
        room.joinPlayer(guest);
        room.setMiniGame(List.of(new MiniGame()));

        // when
        service.playMiniGame(host, room);

        // then
        assertThat(room.isInPlayingState()).isTrue();
    }

    @Test
    void 호스트가_아니면_미니게임_시작_불가() {
        // given
        room.joinPlayer(guest);
        room.setMiniGame(List.of(new MiniGame()));

        // when & then
        assertThatThrownBy(() -> service.playMiniGame(guest, room))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 미니게임_없으면_시작_불가() {
        // given
        room.joinPlayer(guest);

        // when & then
        assertThatThrownBy(() -> service.playMiniGame(host, room))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 플레이어_부족하면_시작_불가() {
        // given
        room.setMiniGame(List.of(new MiniGame()));

        // when & then
        assertThatThrownBy(() -> service.playMiniGame(host, room))
                .isInstanceOf(IllegalStateException.class);
    }
}

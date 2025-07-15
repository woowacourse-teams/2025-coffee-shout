package coffeeshout.coffeeshout.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.coffeeshout.domain.JoinCode;
import coffeeshout.coffeeshout.domain.Menu;
import coffeeshout.coffeeshout.domain.Room;
import coffeeshout.coffeeshout.domain.Roulette;
import coffeeshout.coffeeshout.domain.player.Player;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoulettePlayServiceTest {

    private RoulettePlayService service = new RoulettePlayService();
    private List<Player> players = List.of(new Player(null, "꾹", null, null), new Player(null, "한스", null, null));
    private Room room = new Room(new JoinCode("XYZ123"), new Roulette(players, 1));
    private Player host = new Player(1L, "host", new Menu(), room);
    private Player guest = new Player(2L, "guest", new Menu(), room);

    @BeforeEach
    void setUp() {
        room.changeHost(host);
        room.joinPlayer(host);
    }

    @Test
    void 룰렛_정상_시작_조건_충족() {
        // given
        room.joinPlayer(guest);
        room.setPlaying();

        // when
        service.playRoulette(host, room);

        // then
        assertThat(room.isInPlayingState()).isTrue(); // 상태 유지 확인
    }

    @Test
    void 호스트가_아니면_룰렛_시작_불가() {
        // given
        room.joinPlayer(guest);
        room.setPlaying();

        // when & then
        assertThatThrownBy(() -> service.playRoulette(guest, room))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 플레이어_수_부족하면_룰렛_시작_불가() {
        // given: 플레이어 1명만 참여한 상태
        room.setPlaying();

        // when & then
        assertThatThrownBy(() -> service.playRoulette(host, room))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 방_상태가_PLAYING_이_아니면_룰렛_시작_불가() {
        // given
        room.joinPlayer(guest);
        // 상태는 READY

        // when & then
        assertThatThrownBy(() -> service.playRoulette(host, room))
                .isInstanceOf(IllegalStateException.class);
    }
}

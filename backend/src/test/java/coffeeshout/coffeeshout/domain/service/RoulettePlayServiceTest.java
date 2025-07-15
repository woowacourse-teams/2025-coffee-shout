package coffeeshout.coffeeshout.domain.service;

import coffeeshout.coffeeshout.domain.*;
import coffeeshout.coffeeshout.domain.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RoulettePlayServiceTest {

    private final RoulettePlayService service = new RoulettePlayService();
    private final Room room = new Room(new JoinCode("XYZ123"), new Roulette());
    private final Player host = new Player(1L, "host", new Menu(), room);
    private final Player guest = new Player(2L, "guest", new Menu(), room);

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

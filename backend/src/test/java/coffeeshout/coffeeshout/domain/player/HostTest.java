package coffeeshout.coffeeshout.domain.player;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.coffeeshout.domain.JoinCode;
import coffeeshout.coffeeshout.domain.Menu;
import coffeeshout.coffeeshout.domain.MiniGame;
import coffeeshout.coffeeshout.domain.Room;
import coffeeshout.coffeeshout.domain.Roulette;
import org.junit.jupiter.api.Test;

class HostTest {

    private Menu menu = new Menu();
    private Roulette roulette = new Roulette();
    private Room room = new Room(new JoinCode("ABCD34"), roulette);
    private Host host = new Host(null, "host", menu, room);

    @Test
    void 플레이어_수가_충분하지_않으면_미니게임_시작_불가() {
        // when & then
        assertThatThrownBy(() -> host.playMiniGame())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 미니게임이_없으면_미니게임_시작_불가() {
        // given
        room.joinPlayer(host);
        room.joinPlayer(new Guest(3L, "p2", menu, room));
        // miniGames 없음

        // when & then
        assertThatThrownBy(() -> host.playMiniGame())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 조건_충족시_미니게임_시작_가능하며_상태가_PLAYING으로_변경된다() {
        // given
        room.joinPlayer(host);
        room.joinPlayer(new Guest(3L, "p2", menu, room));
        room.addMiniGame(new MiniGame());

        // when
        host.playMiniGame();

        // then
        assertThat(room.isInPlayingState()).isTrue();
    }

    @Test
    void 게임_상태가_PLAYING이_아니면_룰렛을_시작할_수_없다() {
        // given
        room.joinPlayer(host);
        room.joinPlayer(new Guest(3L, "p2", menu, room));
        room.addMiniGame(new MiniGame());

        // when & then
        assertThatThrownBy(() -> host.playRoulette())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 게임_상태가_PLAYING이고_플레이어가_충분하면_룰렛_가능() {
        // given
        room.joinPlayer(host);
        room.joinPlayer(new Guest(3L, "p2", menu, room));
        room.addMiniGame(new MiniGame());
        room.setPlaying();

        // when & then
        // TODO: 적절한 응답값으로 검증하기
        assertThatCode(() -> host.playRoulette()).doesNotThrowAnyException();
    }
}

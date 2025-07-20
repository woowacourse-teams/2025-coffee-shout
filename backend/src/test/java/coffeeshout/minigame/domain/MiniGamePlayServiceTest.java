//package coffeeshout.minigame.domain;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//import coffeeshout.fixture.PlayerFixture;
//import coffeeshout.fixture.RoomFixture;
//import coffeeshout.player.domain.Player;
//import coffeeshout.room.domain.Room;
//import java.util.List;
//import org.junit.jupiter.api.Test;
//
//class MiniGamePlayServiceTest {
//
//    private MiniGamePlayService service = new MiniGamePlayService();
//    private Room room = RoomFixture.호스트_꾹이();
//    private Player host = room.getHost();
//
/// /    @Test /    void 미니게임_조건_만족시_정상_시작() { /        // given /        Player 한스 = PlayerFixture.한스(); / /
/// room.joinPlayer(한스); /        room.setMiniGame(List.of(new MiniGame())); / /        // when /
/// service.playMiniGame(host, room); / /        // then /        assertThat(room.isInPlayingState()).isTrue(); /    }
//
//    @Test
//    void 호스트가_아니면_미니게임_시작_불가() {
//        // given
//        Player 한스 = PlayerFixture.한스();
//
//        room.joinGuest(한스);
//        room.setMiniGame(List.of(new MiniGame()));
//
//        // when & then
//        assertThatThrownBy(() -> service.playMiniGame(한스, room))
//                .isInstanceOf(IllegalStateException.class);
//    }
//
//    @Test
//    void 미니게임_없으면_시작_불가() {
//        // given
//        Player 한스 = PlayerFixture.한스();
//        room.joinGuest(한스);
//
//        // when & then
//        assertThatThrownBy(() -> service.playMiniGame(host, room))
//                .isInstanceOf(IllegalStateException.class);
//    }
//
//    @Test
//    void 플레이어_부족하면_시작_불가() {
//        // given
//        room.setMiniGame(List.of(new MiniGame()));
//
//        // when & then
//        assertThatThrownBy(() -> service.playMiniGame(host, room))
//                .isInstanceOf(IllegalStateException.class);
//    }
//}

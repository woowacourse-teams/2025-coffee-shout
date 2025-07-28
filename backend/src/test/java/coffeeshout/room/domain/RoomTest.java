package coffeeshout.room.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.fixture.MenuFixture;
import coffeeshout.fixture.MiniGameDummy;
import coffeeshout.fixture.RouletteFixture;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.roulette.Roulette;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RoomTest {

    private final JoinCode joinCode = new JoinCode("ABCDF");
    private final Roulette roulette = RouletteFixture.고정_끝값_반환();
    private final PlayerName 호스트_한스 = new PlayerName("한스");
    private final PlayerName 게스트_루키 = new PlayerName("루키");
    private final PlayerName 게스트_꾹이 = new PlayerName("꾹이");
    private final PlayerName 게스트_엠제이 = new PlayerName("엠제이");

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room(joinCode, 호스트_한스, MenuFixture.아메리카노());
        ReflectionTestUtils.setField(room, "roulette", roulette);
    }

    @Test
    void 방_생성시_상태는_READY이고_호스트가_추가된다() {
        // given
        // when & then
        assertThat(room.getRoomState()).isEqualTo(RoomState.READY);
        assertThat(room.getHost()).isEqualTo(new Player(호스트_한스));
    }

    @Test
    void READY_상태에서는_플레이어가_참여할_수_있다() {
        // given
        room.joinGuest(게스트_꾹이, MenuFixture.아메리카노());

        // when & then
        assertThat(room.getPlayers()).hasSize(2);
    }

    @Test
    void READY_상태가_아니면_참여할_수_없다() {
        // given
        room.joinGuest(게스트_꾹이, MenuFixture.아메리카노());
        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);

        // when & then
        assertThatThrownBy(() -> room.joinGuest(게스트_엠제이, MenuFixture.아메리카노()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 미니게임은_5개_이하여야_한다() {
        // given
        List<Playable> miniGames = new LinkedList<>(List.of(
                new MiniGameDummy(),
                new MiniGameDummy(),
                new MiniGameDummy(),
                new MiniGameDummy()
        ));
        ReflectionTestUtils.setField(room, "miniGames", miniGames);

        // when
        room.addMiniGame(호스트_한스, new MiniGameDummy());

        // then
        assertThat(room.getMiniGames()).hasSize(5);
    }

    @Test
    void 미니게임이_6개_이상이면_예외가_발생한다() {
        // given
        List<Playable> miniGames = new LinkedList<>(List.of(
                new MiniGameDummy(),
                new MiniGameDummy(),
                new MiniGameDummy(),
                new MiniGameDummy(),
                new MiniGameDummy(),
                new MiniGameDummy()
        ));

        ReflectionTestUtils.setField(room, "miniGames", miniGames);

        MiniGameDummy miniGameDummy = new MiniGameDummy();
        // when & then
        assertThatThrownBy(() -> room.addMiniGame(호스트_한스, miniGameDummy))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 미니게임을_제거한다() {
        // given
        CardGame cardGame = new CardGame(new CardGameRandomDeckGenerator());
        room.addMiniGame(호스트_한스, cardGame);

        // when
        room.removeMiniGame(호스트_한스, cardGame);

        // then
        assertThat(room.getMiniGames()).isEmpty();
    }

    @Test
    void 해당_미니게임이_없을_때_제거하면_예외를_발생한다() {
        // given
        CardGame cardGame = new CardGame(new CardGameRandomDeckGenerator());
        room.addMiniGame(호스트_한스, cardGame);
        MiniGameDummy miniGameDummy = new MiniGameDummy();

        // when & then
        assertThatThrownBy(() -> {
            room.removeMiniGame(호스트_한스, miniGameDummy);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 룰렛을_시작하면_상태가_DONE으로_변하고_한_명은_선택된다() {
        // given
        room.joinGuest(게스트_꾹이, MenuFixture.아메리카노());
        room.joinGuest(게스트_루키, MenuFixture.아메리카노());
        room.joinGuest(게스트_엠제이, MenuFixture.아메리카노());

        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);
        Player winner = room.startRoulette();

        // when & then
        assertThat(room.getRoomState()).isEqualTo(RoomState.DONE);
        assertThat(winner).isEqualTo(new Player(new PlayerName("한스"), MenuFixture.아메리카노()));
    }

    @Test
    void 룰렛은_2명_이상이어야_돌릴_수_있다() {
        // given

        // when & then
        assertThatThrownBy(() -> room.startRoulette())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 룰렛은_게임_중일때만_돌릴_수_있다() {
        // given
        room.joinGuest(게스트_꾹이, MenuFixture.아메리카노());

        // when & then
        assertThatThrownBy(() -> room.startRoulette())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 호스트_판별이_가능하다() {
        // given

        // when & then
        assertThat(room.isHost(new Player(호스트_한스, MenuFixture.아메리카노()))).isTrue();
        assertThat(room.isHost(new Player(게스트_꾹이, MenuFixture.아메리카노()))).isFalse();
    }

    @Test
    void 호스트가_아니면_미니게임을_추가할_수_없다() {
        // given
        room.joinGuest(게스트_꾹이, MenuFixture.아메리카노());
        MiniGameDummy miniGameDummy = new MiniGameDummy();

        // when & then
        assertThatThrownBy(() -> room.addMiniGame(게스트_꾹이, miniGameDummy))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 호스트가_아니면_미니게임을_제거할_수_없다() {
        // given
        CardGame cardGame = new CardGame(new CardGameRandomDeckGenerator());
        room.addMiniGame(호스트_한스, cardGame);
        room.joinGuest(게스트_꾹이, MenuFixture.아메리카노());

        // when & then
        assertThatThrownBy(() -> room.removeMiniGame(게스트_꾹이, cardGame))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

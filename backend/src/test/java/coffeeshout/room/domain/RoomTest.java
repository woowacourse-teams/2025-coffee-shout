package coffeeshout.room.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.minigame.domain.MiniGame;
import coffeeshout.player.domain.Player;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RoomTest {

    private JoinCode joinCode = new JoinCode("ABCDF");
    private Player 호스트_한스 = PlayerFixture.한스();
    private Player 게스트_루키 = PlayerFixture.루키();
    private Player 게스트_꾹이 = PlayerFixture.꾹이();
    private Player 게스트_엠제이 = PlayerFixture.엠제이();

    private Room room;

    @BeforeEach
    void setUp() {
        PlayerInfos playerInfos = new PlayerInfos(호스트_한스);
        room = new Room(joinCode, playerInfos, new FixedLastValueGenerator());
    }

    @Test
    void 방_생성시_상태는_READY이고_호스트가_추가된다() {
        // given
        // when & then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(room.getRoomState()).isEqualTo(RoomState.READY);
            softly.assertThat(room.getHost()).isEqualTo(호스트_한스);
            softly.assertThat(room.getPlayers().size()).isEqualTo(1);
        });
    }

    @Test
    void READY_상태에서는_플레이어가_참여할_수_있다() {
        // given
        room.joinPlayer(게스트_꾹이);

        // when & then
        assertThat(room.getPlayers().size()).isEqualTo(2);
    }

    @Test
    void READY_상태가_아니면_참여할_수_없다() {
        // given
        room.joinPlayer(게스트_꾹이);
        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);

        // when & then
        assertThatThrownBy(() -> room.joinPlayer(게스트_엠제이))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 미니게임은_5개_이하여야_한다() {
        // given
        List<MiniGame> miniGames = List.of(
                new MiniGame(),
                new MiniGame(),
                new MiniGame(),
                new MiniGame(),
                new MiniGame()
        );

        // when
        room.setMiniGame(miniGames);

        // then
        assertThat(room.getMiniGames()).hasSize(5);
    }

    @Test
    void 미니게임이_6개_이상이면_예외가_발생한다() {
        // given
        List<MiniGame> miniGames = List.of(
                new MiniGame(),
                new MiniGame(),
                new MiniGame(),
                new MiniGame(),
                new MiniGame(),
                new MiniGame()
        );

        // when & then
        assertThatThrownBy(() -> room.setMiniGame(miniGames))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 룰렛을_시작하면_상태가_DONE으로_변하고_한_명은_선택된다() {
        // given
        room.joinPlayer(게스트_꾹이);

        room.joinPlayer(게스트_루키);
        room.joinPlayer(게스트_엠제이);

        ReflectionTestUtils.setField(room, "roomState", RoomState.PLAYING);
        Player loser = room.startRoulette();

        // when & then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(room.getRoomState()).isEqualTo(RoomState.DONE);
            softly.assertThat(loser).isEqualTo(게스트_엠제이);
        });
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
        room.joinPlayer(게스트_꾹이);

        // when & then
        assertThatThrownBy(() -> room.startRoulette())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 호스트_판별이_가능하다() {
        // given

        // when & then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(room.isHost(호스트_한스)).isTrue();
            softly.assertThat(room.isHost(게스트_꾹이)).isFalse();
        });
    }
}

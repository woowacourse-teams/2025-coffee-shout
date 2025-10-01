package coffeeshout.minigame.racinggame.domain;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.room.domain.player.Player;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RacingGameTest {

    @Test
    void 레이싱_게임을_생성할_수_있다() {
        // when
        final RacingGame racingGame = new RacingGame();

        // then
        assertThat(racingGame.getState()).isEqualTo(RacingGameState.READY);
    }

    @Test
    void 게임을_시작할_수_있다() {
        // given
        final RacingGame racingGame = new RacingGame();
        final List<Player> players = List.of(PlayerFixture.호스트피케이(), PlayerFixture.게스트한스());

        // when
        racingGame.startGame(players);

        // then
        assertThat(racingGame.getState()).isEqualTo(RacingGameState.PLAYING);
        assertThat(racingGame.getPositions()).hasSize(2);
    }

    @Test
    void 게임이_진행_중이_아니면_속도_조정시_예외가_발생한다() {
        // given
        final RacingGame racingGame = new RacingGame();
        final Player player = PlayerFixture.게스트한스();
        final Instant timestamp = Instant.now();

        // when && then
        assertThatThrownBy(() -> racingGame.adjustSpeed(player, 10, timestamp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("게임이 진행 중이 아닙니다.");
    }

    @Test
    void 플레이어의_속도를_조정할_수_있다() {
        // given
        final Player player = PlayerFixture.게스트한스();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player));
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(1000);

        // when
        racingGame.adjustSpeed(player, 8, secondTime); // 1초에 8번 클릭

        // then
        assertThat(racingGame.getSpeeds().get(player)).isEqualTo(8);
    }

    @Test
    void 모든_러너를_이동시킬_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트피케이();
        final Player player2 = PlayerFixture.게스트한스();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player1, player2));

        // when
        racingGame.moveAll();

        // then
        assertThat(racingGame.getPositions().get(player1)).isEqualTo(5);
        assertThat(racingGame.getPositions().get(player2)).isEqualTo(5);
    }

    @Test
    void 우승자가_나오면_게임이_종료된다() {
        // given
        final Player player = PlayerFixture.게스트한스();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player));
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(100);

        racingGame.adjustSpeed(player, 10, secondTime); // speed = 10

        // when
        for (int i = 0; i < 100; i++) {
            racingGame.moveAll();
        }

        // then
        assertThat(racingGame.getState()).isEqualTo(RacingGameState.FINISHED);
        assertThat(racingGame.isFinished()).isTrue();
    }

    @Test
    void 게임_타입은_RACING_GAME이다() {
        // given
        final RacingGame racingGame = new RacingGame();

        // when && then
        assertThat(racingGame.getMiniGameType()).isEqualTo(MiniGameType.RACING_GAME);
    }

    @Test
    void 모든_플레이어의_위치를_조회할_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트피케이();
        final Player player2 = PlayerFixture.게스트한스();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player1, player2));
        racingGame.moveAll();

        // when
        final Map<Player, Integer> positions = racingGame.getPositions();

        // then
        assertThat(positions).hasSize(2);
        assertThat(positions.get(player1)).isEqualTo(5);
        assertThat(positions.get(player2)).isEqualTo(5);
    }

    @Test
    void 모든_플레이어의_속도를_조회할_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트피케이();
        final Player player2 = PlayerFixture.게스트한스();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player1, player2));
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(1000);

        racingGame.adjustSpeed(player1, 9, secondTime);

        // when
        final Map<Player, Integer> speeds = racingGame.getSpeeds();

        // then
        assertThat(speeds).hasSize(2);
        assertThat(speeds.get(player1)).isEqualTo(9);
        assertThat(speeds.get(player2)).isEqualTo(5);
    }

    @Test
    void 게임_결과를_조회할_수_있다() {
        // given
        final Player player = PlayerFixture.게스트한스();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player));
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(100);

        racingGame.adjustSpeed(player, 10, secondTime);

        // when && then
        assertThat(racingGame.getResult()).isNotNull();
        assertThat(racingGame.getScores()).hasSize(1);
    }

    @Test
    void 게임_순위를_조회할_수_있다() throws InterruptedException {
        // given
        final Player player1 = PlayerFixture.호스트피케이();
        final Player player2 = PlayerFixture.게스트한스();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player1, player2));
        final Instant firstTime = Instant.now();
        final Instant secondTime = firstTime.plusMillis(100);

        racingGame.adjustSpeed(player1, 10, secondTime);

        // 게임이 자동으로 진행될 때까지 대기
        Thread.sleep(11000); // 100ms * 100회 = 10초 + 여유

        // when
        final List<Player> ranking = racingGame.getRanking();

        // then
        assertThat(ranking).isNotEmpty();
        assertThat(racingGame.isFinished()).isTrue();
    }
}

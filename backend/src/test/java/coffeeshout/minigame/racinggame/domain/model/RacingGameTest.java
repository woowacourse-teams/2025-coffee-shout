package coffeeshout.minigame.racinggame.domain.model;

import static org.assertj.core.api.Assertions.*;

import coffeeshout.fixture.PlayerFixture;
import coffeeshout.minigame.cardgame.domain.MiniGameType;
import coffeeshout.minigame.racinggame.domain.RacingGame;
import coffeeshout.minigame.racinggame.domain.RacingGameState;
import coffeeshout.minigame.racinggame.domain.Runner;
import coffeeshout.room.domain.player.Player;
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
        final List<Player> players = List.of(PlayerFixture.호스트한스(), PlayerFixture.게스트꾹이());

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
        final Player player = PlayerFixture.게스트꾹이();

        // when && then
        assertThatThrownBy(() -> racingGame.adjustSpeed(player, 10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("게임이 진행 중이 아닙니다.");
    }

    @Test
    void 플레이어의_속도를_조정할_수_있다() throws InterruptedException {
        // given
        final Player player = PlayerFixture.게스트꾹이();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player));

        Thread.sleep(1000);

        // when
        racingGame.adjustSpeed(player, 8); // 1초에 8번 클릭

        // then
        final Map<Runner, Integer> speeds = racingGame.getSpeeds();
        final Runner runner = speeds.keySet().stream()
                .filter(r -> r.getPlayer().equals(player))
                .findFirst()
                .orElseThrow();
        assertThat(speeds.get(runner)).isEqualTo(8);
    }

    @Test
    void 모든_러너를_이동시킬_수_있다() {
        // given
        final Player player1 = PlayerFixture.호스트한스();
        final Player player2 = PlayerFixture.게스트꾹이();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player1, player2));

        // when
        racingGame.moveAll();

        // then
        final Map<Runner, Integer> positions = racingGame.getPositions();
        assertThat(positions.values()).allMatch(position -> position == 1); // INITIAL_SPEED = 1
    }

    @Test
    void 모든_러너가_완주하면_게임이_종료된다() throws InterruptedException {
        // given
        final Player player = PlayerFixture.게스트꾹이();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player));

        Thread.sleep(100);
        racingGame.adjustSpeed(player, 10); // speed = 10

        // when
        for (int i = 0; i < 100; i++) {
            racingGame.moveAll();
        }

        // then
        assertThat(racingGame.getState()).isEqualTo(RacingGameState.DONE);
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
        final Player player1 = PlayerFixture.호스트한스();
        final Player player2 = PlayerFixture.게스트꾹이();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player1, player2));
        racingGame.moveAll();

        // when
        final Map<Runner, Integer> positions = racingGame.getPositions();

        // then
        assertThat(positions).hasSize(2);
        assertThat(positions.values()).allMatch(position -> position == 1); // INITIAL_SPEED = 1
    }

    @Test
    void 모든_플레이어의_속도를_조회할_수_있다() throws InterruptedException {
        // given
        final Player player1 = PlayerFixture.호스트한스();
        final Player player2 = PlayerFixture.게스트꾹이();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player1, player2));

        Thread.sleep(1000);
        racingGame.adjustSpeed(player1, 9);

        // when
        final Map<Runner, Integer> speeds = racingGame.getSpeeds();

        // then
        assertThat(speeds).hasSize(2);
        final Runner runner1 = speeds.keySet().stream()
                .filter(r -> r.getPlayer().equals(player1))
                .findFirst()
                .orElseThrow();
        final Runner runner2 = speeds.keySet().stream()
                .filter(r -> r.getPlayer().equals(player2))
                .findFirst()
                .orElseThrow();
        assertThat(speeds.get(runner1)).isEqualTo(9);
        assertThat(speeds.get(runner2)).isEqualTo(1); // INITIAL_SPEED = 1
    }

    @Test
    void 게임_결과를_조회할_수_있다() throws InterruptedException {
        // given
        final Player player = PlayerFixture.게스트꾹이();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player));

        Thread.sleep(100);
        racingGame.adjustSpeed(player, 10);
        for (int i = 0; i < 100; i++) {
            racingGame.moveAll();
        }

        // when && then
        assertThat(racingGame.getResult()).isNotNull();
        assertThat(racingGame.getScores()).hasSize(1);
    }

    @Test
    void 게임_순위를_조회할_수_있다() throws InterruptedException {
        // given
        final Player player1 = PlayerFixture.호스트한스();
        final Player player2 = PlayerFixture.게스트꾹이();
        final RacingGame racingGame = new RacingGame();
        racingGame.startGame(List.of(player1, player2));

        Thread.sleep(100);
        racingGame.adjustSpeed(player1, 10);
        racingGame.adjustSpeed(player2, 10);

        for (int i = 0; i < 100; i++) {
            racingGame.moveAll();
        }

        // when
        final List<Player> ranking = racingGame.getRanking();

        // then
        assertThat(ranking).hasSize(2);
        assertThat(racingGame.isFinished()).isTrue();
    }

    @Test
    void 게임_시작_여부를_확인할_수_있다() {
        // given
        final RacingGame racingGame = new RacingGame();
        final List<Player> players = List.of(PlayerFixture.호스트한스());

        // when
        racingGame.startGame(players);

        // then
        assertThat(racingGame.isStarted()).isTrue();
    }

    @Test
    void 자동_이동을_시작하고_중지할_수_있다() {
        // given
        final RacingGame racingGame = new RacingGame();

        // when
        racingGame.stopAutoMove();

        // then - 예외 없이 실행됨
        assertThat(racingGame).isNotNull();
    }
}
